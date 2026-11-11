package com.trhsy.sim.gui;

import com.trhsy.sim.loader.BlockLoader;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 市长手册：给第一次接触模拟城市的玩家一个随时可查的玩法入口。
 *
 * <p>内容只讲“下一步做什么”，不堆设定。页面用深色面板，正文自动换行，
 * 小窗口下也不会把字画出屏幕。第 7–8 页是配方速查，直接画 3×3 合成格。
 */
public class GuiCityGuide extends GuiScreen {
    private static final String[][] PAGES = new String[][]{
            {
                    "container.sim.guide.page.0.title",
                    "container.sim.guide.page.0.0",
                    "container.sim.guide.page.0.1",
                    "container.sim.guide.page.0.2",
                    "container.sim.guide.page.0.3"
            },
            {
                    "container.sim.guide.toc.title",
                    "container.sim.guide.toc.0",
                    "container.sim.guide.toc.1",
                    "container.sim.guide.toc.2",
                    "container.sim.guide.toc.3",
                    "container.sim.guide.toc.4",
                    "container.sim.guide.toc.5",
                    "container.sim.guide.toc.6",
                    "container.sim.guide.toc.7"
            },
            {
                    "container.sim.guide.page.1.title",
                    "container.sim.guide.page.1.0",
                    "container.sim.guide.page.1.1",
                    "container.sim.guide.page.1.2",
                    "container.sim.guide.page.1.3",
                    "container.sim.guide.page.1.4"
            },
            {
                    "container.sim.guide.page.2.title",
                    "container.sim.guide.page.2.0",
                    "container.sim.guide.page.2.1",
                    "container.sim.guide.page.2.2",
                    "container.sim.guide.page.2.3",
                    "container.sim.guide.page.2.4",
                    "container.sim.guide.page.2.5"
            },
            {
                    "container.sim.guide.page.8.title",
                    "container.sim.guide.page.8.0",
                    "container.sim.guide.page.8.1",
                    "container.sim.guide.page.8.2",
                    "container.sim.guide.page.8.3",
                    "container.sim.guide.page.8.4"
            },
            {
                    "container.sim.guide.materials.title",
                    "container.sim.guide.materials.0",
                    "container.sim.guide.materials.1",
                    "container.sim.guide.materials.2",
                    "container.sim.guide.materials.3",
                    "container.sim.guide.materials.4",
                    "container.sim.guide.materials.5"
            },
            {
                    "container.sim.guide.recipes.title",
                    "container.sim.guide.recipes.0"
            },
            {
                    "container.sim.guide.recipes.title2",
                    "container.sim.guide.recipes.1"
            },
            {
                    "container.sim.guide.page.3.title",
                    "container.sim.guide.page.3.0",
                    "container.sim.guide.page.3.1",
                    "container.sim.guide.page.3.2",
                    "container.sim.guide.page.3.3",
                    "container.sim.guide.page.3.4"
            },
            {
                    "container.sim.guide.page.5.title",
                    "container.sim.guide.page.5.0",
                    "container.sim.guide.page.5.1",
                    "container.sim.guide.page.5.2",
                    "container.sim.guide.page.5.3",
                    "container.sim.guide.page.5.4"
            },
            {
                    "container.sim.guide.page.4.title",
                    "container.sim.guide.page.4.0",
                    "container.sim.guide.page.4.1",
                    "container.sim.guide.page.4.2",
                    "container.sim.guide.page.4.3",
                    "container.sim.guide.page.4.4"
            },
            {
                    "container.sim.guide.mining.title",
                    "container.sim.guide.mining.0",
                    "container.sim.guide.mining.1",
                    "container.sim.guide.mining.2",
                    "container.sim.guide.mining.3",
                    "container.sim.guide.mining.4"
            },
            {
                    "container.sim.guide.page.9.title",
                    "container.sim.guide.page.9.0",
                    "container.sim.guide.page.9.1",
                    "container.sim.guide.page.9.2",
                    "container.sim.guide.page.9.3",
                    "container.sim.guide.page.9.4"
            },
            {
                    "container.sim.guide.shops.title",
                    "container.sim.guide.shops.0",
                    "container.sim.guide.shops.1",
                    "container.sim.guide.shops.2",
                    "container.sim.guide.shops.3",
                    "container.sim.guide.shops.4",
                    "container.sim.guide.shops.5",
                    "container.sim.guide.shops.6"
            },
            {
                    "container.sim.guide.npc.title",
                    "container.sim.guide.npc.0",
                    "container.sim.guide.npc.1",
                    "container.sim.guide.npc.2",
                    "container.sim.guide.npc.3",
                    "container.sim.guide.npc.4",
                    "container.sim.guide.npc.5"
            },
            {
                    "container.sim.guide.page.10.title",
                    "container.sim.guide.page.10.0",
                    "container.sim.guide.page.10.1",
                    "container.sim.guide.page.10.2",
                    "container.sim.guide.page.10.3",
                    "container.sim.guide.page.10.4"
            },
            {
                    "container.sim.guide.page.6.title",
                    "container.sim.guide.page.6.0",
                    "container.sim.guide.page.6.1",
                    "container.sim.guide.page.6.2",
                    "container.sim.guide.page.6.3",
                    "container.sim.guide.page.6.4",
                    "container.sim.guide.page.6.5",
                    "container.sim.guide.page.6.6"
            },
            {
                    "container.sim.guide.page.7.title",
                    "container.sim.guide.page.7.0",
                    "container.sim.guide.page.7.1",
                    "container.sim.guide.page.7.2",
                    "container.sim.guide.page.7.3",
                    "container.sim.guide.page.7.4",
                    "container.sim.guide.page.7.5"
            }
    };

    /** 配方速查页在 PAGES 里的下标。 */
    private static final int RECIPE_PAGE = 6;
    private static final int RECIPE_PAGE_2 = 7;

    /** 一张配方卡：3×3 摆放 + 产物。 */
    private static final class RecipeCard {
        final String titleKey;
        final String noteKey;
        final ItemStack[] grid;
        final ItemStack output;

        RecipeCard(String titleKey, String noteKey, ItemStack output, ItemStack... grid) {
            this.titleKey = titleKey;
            this.noteKey = noteKey;
            this.output = output;
            this.grid = new ItemStack[9];
            for (int i = 0; i < 9; i++) {
                this.grid[i] = i < grid.length && grid[i] != null ? grid[i] : ItemStack.EMPTY;
            }
        }
    }

    private static final List<RecipeCard> RECIPES = new ArrayList<RecipeCard>();

    static {
        ItemStack planks = new ItemStack(Blocks.PLANKS, 1, 0);
        ItemStack cobble = new ItemStack(Blocks.COBBLESTONE);
        ItemStack table = new ItemStack(Blocks.CRAFTING_TABLE);
        ItemStack stick = new ItemStack(Items.STICK);
        ItemStack yellowDye = new ItemStack(Items.DYE, 1, 11);

        RECIPES.add(new RecipeCard(
                "container.sim.guide.recipes.constructor",
                "container.sim.guide.recipes.note.planks",
                new ItemStack(BlockLoader.blockConstructorBox),
                planks, planks, planks,
                cobble, table, cobble,
                cobble, cobble, cobble));

        RECIPES.add(new RecipeCard(
                "container.sim.guide.recipes.marker",
                "container.sim.guide.recipes.note.marker",
                new ItemStack(BlockLoader.blockMarker),
                ItemStack.EMPTY, yellowDye, ItemStack.EMPTY,
                ItemStack.EMPTY, stick, ItemStack.EMPTY,
                ItemStack.EMPTY, stick, ItemStack.EMPTY));

        RECIPES.add(new RecipeCard(
                "container.sim.guide.recipes.farming",
                "container.sim.guide.recipes.note.hoe",
                new ItemStack(BlockLoader.blockFarmingBox),
                planks, planks, planks,
                cobble, new ItemStack(Items.IRON_HOE), cobble,
                cobble, cobble, cobble));

        RECIPES.add(new RecipeCard(
                "container.sim.guide.recipes.mining",
                "container.sim.guide.recipes.note.pickaxe",
                new ItemStack(BlockLoader.blockMiningBox),
                planks, planks, planks,
                cobble, new ItemStack(Items.IRON_PICKAXE), cobble,
                cobble, cobble, cobble));
    }

    private final GuiScreen parent;
    private int page = 0;
    private int contentX;
    private int contentW;
    private int contentBottom;
    private ItemStack hoveredStack = ItemStack.EMPTY;

    public GuiCityGuide() {
        this(null);
    }

    public GuiCityGuide(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.contentW = Math.min(440, Math.max(240, this.width - 40));
        this.contentX = (this.width - this.contentW) / 2;
        this.contentBottom = this.height - 48;

        int buttonW = 72;
        int gap = 6;
        int tocW = 64;
        int closeW = 64;
        int totalW = buttonW * 2 + tocW + closeW + gap * 3;
        int startX = (this.width - totalW) / 2;
        int buttonY = this.height - 28;

        GuiButton prev = new GuiButton(0, startX, buttonY, buttonW, 20,
                I18n.format("container.sim.guide.prev"));
        prev.enabled = this.page > 0;
        this.buttonList.add(prev);

        GuiButton next = new GuiButton(1, startX + buttonW + gap, buttonY, buttonW, 20,
                I18n.format("container.sim.guide.next"));
        next.enabled = this.page < PAGES.length - 1;
        this.buttonList.add(next);

        this.buttonList.add(new GuiButton(3, startX + buttonW * 2 + gap * 2, buttonY, tocW, 20,
                I18n.format("container.sim.guide.toc")));

        this.buttonList.add(new GuiButton(2, startX + buttonW * 2 + tocW + gap * 3, buttonY, closeW, 20,
                I18n.format("container.sim.guide.close")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        String title = this.t(PAGES[this.page][0]);
        GuiStyle.text(this.fontRenderer, (this.width - this.fontRenderer.getStringWidth(title)) / 2, 15,
                title, GuiStyle.WHITE);
        GuiStyle.textRight(this.fontRenderer, this.width - 10, 15,
                I18n.format("container.sim.guide.page", this.page + 1, PAGES.length), GuiStyle.LIGHT_MUTED);

        this.hoveredStack = ItemStack.EMPTY;
        if (this.page == RECIPE_PAGE || this.page == RECIPE_PAGE_2) {
            this.drawRecipePage(this.page == RECIPE_PAGE ? 0 : 2, mouseX, mouseY);
        } else {
            int y = 40;
            boolean overflow = false;
            for (int i = 1; i < PAGES[this.page].length && !overflow; i++) {
                List<String> lines = GuiLayout.wrap(this.fontRenderer, this.t(PAGES[this.page][i]), this.contentW);
                for (String line : lines) {
                    if (y > this.contentBottom - 10) {
                        overflow = true;
                        break;
                    }
                    GuiStyle.text(this.fontRenderer, this.contentX, y, line, GuiStyle.WHITE);
                    y += 10;
                }
                y += 3;
            }
        }

        String hint = I18n.format("container.sim.guide.hint");
        GuiStyle.text(this.fontRenderer, (this.width - this.fontRenderer.getStringWidth(hint)) / 2,
                this.height - 42, hint, GuiStyle.LIGHT_MUTED);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.hoveredStack != null && !this.hoveredStack.isEmpty()) {
            this.renderToolTip(this.hoveredStack, mouseX, mouseY);
        }
    }

    /** 配方速查：每页 2 张 3×3 卡片，共 2 页。 */
    private void drawRecipePage(int startIndex, int mouseX, int mouseY) {
        GuiStyle.text(this.fontRenderer, this.contentX, 28,
                GuiLayout.truncate(this.fontRenderer, this.t(PAGES[this.page][1]), this.contentW),
                GuiStyle.LIGHT_MUTED);

        int gap = 6;
        int top = 42;
        int cardH = (this.contentBottom - top - gap) / 2;
        if (cardH < 72) {
            GuiStyle.text(this.fontRenderer, this.contentX, top + 16,
                    GuiLayout.truncate(this.fontRenderer, this.t("container.sim.guide.recipes.too_small"), this.contentW),
                    0xFFFF8080);
            return;
        }
        for (int i = 0; i < 2 && startIndex + i < RECIPES.size(); i++) {
            int y = top + i * (cardH + gap);
            this.drawRecipeCard(RECIPES.get(startIndex + i), this.contentX, y, this.contentW, cardH, mouseX, mouseY);
        }
    }

    private void drawRecipeCard(RecipeCard recipe, int x, int y, int w, int h, int mouseX, int mouseY) {
        GuiStyle.text(this.fontRenderer, x + 2, y + 2,
                GuiLayout.truncate(this.fontRenderer, this.t(recipe.titleKey), w - 4), GuiStyle.WHITE);

        int gridX = x + 2;
        int gridY = y + 13;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = gridX + col * 18;
                int sy = gridY + row * 18;
                GuiStyle.slot(sx, sy);
                ItemStack stack = recipe.grid[row * 3 + col];
                if (stack != null && !stack.isEmpty()) {
                    this.drawItem(stack, sx + 1, sy + 1);
                    this.rememberHover(stack, sx, sy, mouseX, mouseY);
                }
            }
        }

        int arrowX = gridX + 58;
        int slotX = gridX + 68;
        GuiStyle.text(this.fontRenderer, arrowX, gridY + 22, ">", GuiStyle.LIGHT_MUTED);
        GuiStyle.slot(slotX, gridY + 18);
        this.drawItem(recipe.output, slotX + 1, gridY + 19);
        this.rememberHover(recipe.output, slotX, gridY + 18, mouseX, mouseY);

        int noteX = slotX + 24;
        int noteW = Math.max(20, x + w - noteX - 2);
        List<String> noteLines = GuiLayout.wrap(this.fontRenderer, this.t(recipe.noteKey), noteW);
        int noteY = gridY + 4;
        for (int i = 0; i < noteLines.size() && i < 4 && noteY + i * 10 < y + h - 2; i++) {
            GuiStyle.text(this.fontRenderer, noteX, noteY + i * 10, noteLines.get(i), GuiStyle.LIGHT_MUTED);
        }
    }

    private void drawItem(ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, x, y, null);
        RenderHelper.disableStandardItemLighting();
    }

    private void rememberHover(ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
            this.hoveredStack = stack;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0 && this.page > 0) {
            this.page--;
            this.initGui();
        } else if (button.id == 1 && this.page < PAGES.length - 1) {
            this.page++;
            this.initGui();
        } else if (button.id == 3 && this.page != 1) {
            this.page = 1;
            this.initGui();
        } else if (button.id == 2) {
            this.closeGuide();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            this.closeGuide();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void closeGuide() {
        if (this.parent != null) {
            this.mc.displayGuiScreen(this.parent);
        } else {
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
    }

    private String t(String key) {
        return new TextComponentTranslation(key).getUnformattedText();
    }
}
