package com.trhsy.sim.gui;

import com.trhsy.sim.loader.ModSimClientLoader;
import com.trhsy.sim.loader.ModSimLoader;
import com.trhsy.sim.util.SeasonCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author Trhsy
 * @Package: com.trhsy.sim.gui
 * @ClassName: GuiHud
 * @Description:
 * @date 2023/11/20 下午 3:03
 */
public class GuiHud extends GuiScreen {
    public GuiHud() {
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    /**
     * 初始化
     */
    @Override
    public void initGui() {
        try {
            if (this.mc.currentScreen == null) {
                return;
            }
            String worldname = "unknown";
            try {
                worldname = this.mc.getIntegratedServer().getFolderName();
            } catch (Exception var7) {
                worldname = "Server";
            }
            try {
                if (ModSimClientLoader.sim_is_running) {
                    if (ModSimClientLoader.gamemode != 999) {
                        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                            int hudOffset = 0;
                            if (this.mc.player != null && mc.player.dimension == 1) {
                                hudOffset = 20;
                            }

                            String dayText = new TextComponentTranslation("container.sim.title_days",
                                    ModSimClientLoader.gameDay).getUnformattedText();
                            String season = SeasonCompat.getSeasonName(mc.world);
                            String line1 = worldname + " " + dayText + " (" + this.weekdayName() + ")"
                                    + (season.isEmpty() ? "" : " · " + season);
                            String popText = new TextComponentTranslation("container.sim.trhsy3",
                                    new Object[0]).getUnformattedText();
                            String line2 = popText + ": " + ModSimClientLoader.tempHireableNpcNames.size();
                            if (ModSimClientLoader.gamemode != 1) {
                                String moneyText = new TextComponentTranslation("container.sim.trhsy4",
                                        new Object[0]).getUnformattedText();
                                line2 += "   " + moneyText + ": " + ModSimLoader.displayMoney(ModSimClientLoader.money);
                            }
                            this.drawHudText(line1, line2, hudOffset);
                        }
                    } else if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                        this.drawHudText(this.idleHudText(), "", 0);
                    }
                } else {
                    this.drawHudText(this.idleHudText(), "", 0);
                }

            } catch (Exception e) {
                StackTraceElement element = e.getStackTrace()[0];
                ModSimLoader.log.error("renderTick出错了：" + e.getMessage() + "行数：" + element.getLineNumber());
            }

        } catch (Exception e) {
            StackTraceElement element = e.getStackTrace()[0];
            ModSimLoader.log.error("GuiHud-initGui出错了：" + e.getMessage() + "行数：" + element.getLineNumber());
        }
    }

    private String idleHudText() {
        String key = ModSimClientLoader.gamemode == 999
                ? "container.sim.hud.not_started"
                : "container.sim.trhsy5";
        return new TextComponentTranslation(key, new Object[0]).getUnformattedText();
    }

    private String weekdayName() {
        String[] keys = {
                "container.sim.simSun", "container.sim.simMon", "container.sim.simTue",
                "container.sim.simWed", "container.sim.simThu", "container.sim.simFri",
                "container.sim.simSat"
        };
        int index = ModSimClientLoader.dayOfWeek;
        if (index < 0 || index >= keys.length) {
            index = 0;
        }
        return new TextComponentTranslation(keys[index], new Object[0]).getUnformattedText();
    }

    private void drawHudText(String line1, String line2, int offset) {
        GuiStyle.hudText(line1, line2, offset);
    }
}
