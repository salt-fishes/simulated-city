package com.trhsy.sim.util;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Serene Seasons 的可选兼容层。
 *
 * <p>使用反射而不是编译期依赖：没有安装四季模组时，HUD 只是不显示季节，
 * 不会影响整合包单独构建。</p>
 */
public final class SeasonCompat {
    private static final String MOD_ID = "sereneseasons";
    private static final String SEASON_HELPER = "sereneseasons.api.season.SeasonHelper";
    private static final String SEASON_STATE = "sereneseasons.api.season.ISeasonState";

    private static boolean initialized = false;
    private static boolean available = false;
    private static Method getSeasonState;
    private static Method getSubSeason;
    private static Method getSeason;
    private static Method getDay;

    private SeasonCompat() {
    }

    private static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (!Loader.isModLoaded(MOD_ID)) {
            return;
        }
        try {
            Class<?> helper = Class.forName(SEASON_HELPER);
            Class<?> state = Class.forName(SEASON_STATE);
            getSeasonState = helper.getMethod("getSeasonState", World.class);
            getSubSeason = state.getMethod("getSubSeason");
            getSeason = state.getMethod("getSeason");
            getDay = state.getMethod("getDay");
            available = true;
        } catch (Throwable ignored) {
            available = false;
        }
    }

    /** 当前世界是否安装了四季模组并能读到季节。 */
    public static boolean isAvailable() {
        init();
        return available;
    }

    private static Object state(World world) {
        if (!isAvailable() || world == null) {
            return null;
        }
        try {
            return getSeasonState.invoke(null, world);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** 返回当前亚季节的翻译键，例如 container.sim.season.early_summer。 */
    public static String getSeasonKey(World world) {
        Object seasonState = state(world);
        if (seasonState == null) {
            return "";
        }
        try {
            Object subSeason = getSubSeason.invoke(seasonState);
            if (subSeason == null) {
                return "";
            }
            String name = String.valueOf(subSeason).toLowerCase(Locale.ROOT);
            return "container.sim.season." + name;
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** 当前亚季节的中文/英文显示名；读取不到时返回空字符串。 */
    public static String getSeasonName(World world) {
        String key = getSeasonKey(world);
        return key.isEmpty() ? "" : new TextComponentTranslation(key).getUnformattedText();
    }

    /** 当前亚季节第几天（1-7）；读取不到时返回 0。 */
    public static int getSeasonDay(World world) {
        Object seasonState = state(world);
        if (seasonState == null) {
            return 0;
        }
        try {
            Object value = getDay.invoke(seasonState);
            return value instanceof Number ? ((Number) value).intValue() : 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** 当前世界是否处于冬季；没有四季模组时返回 false。 */
    public static boolean isWinter(World world) {
        if (world == null || world.isRemote) {
            return false;
        }
        Object seasonState = state(world);
        if (seasonState == null) {
            return false;
        }
        try {
            Object season = getSeason.invoke(seasonState);
            return season != null && "WINTER".equals(String.valueOf(season));
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** HUD 左侧小圆点的颜色，按季节区分。 */
    public static int getSeasonColor(World world) {
        String key = getSeasonKey(world);
        if (key.isEmpty()) {
            return 0;
        }
        if (key.contains("spring")) {
            return 0xFF7FD48A;
        }
        if (key.contains("summer")) {
            return 0xFFE3BE6A;
        }
        if (key.contains("autumn")) {
            return 0xFFE08A4A;
        }
        if (key.contains("winter")) {
            return 0xFF8EC5FF;
        }
        return 0xFFC8A24A;
    }
}
