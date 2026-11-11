package com.trhsy.sim.gui.block;

import com.trhsy.sim.gui.GuiLayout;
import com.trhsy.sim.gui.GuiStyle;
import com.trhsy.sim.loader.ModSimClientLoader;
import com.trhsy.sim.loader.NetWorkLoader;
import com.trhsy.sim.network.server.PacketRequestBuildingList;
import com.trhsy.sim.network.server.PacketTeleportToBuilding;
import com.trhsy.sim.npcCode.build.BuildingInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 城市建筑传送列表。
 *
 * <p>从建筑箱或建筑控制箱打开，列出所有已建成建筑；选中后点“传送”即可
 * 让服务端把玩家放到该建筑门口。真正的落点校验在服务端做。</p>
 */
public class GuiBuildingTeleportList extends GuiScreen {
    private static final int ROW_H = 22;
    private static final int ROWS_PER_PAGE = 9;

    private final GuiScreen parent;
    private final String highlightId;
    private List<BuildingInfo> buildings = new ArrayList<BuildingInfo>();
    private int offset = 0;
    private String selectedId = "";
    private long lastRequestTime = 0L;
    private long lastClickTime = 0L;
    private int lastClickIndex = -1;

    public GuiBuildingTeleportList(GuiScreen parent) {
        this(parent, null);
    }

    public GuiBuildingTeleportList(GuiScreen parent, String highlightId) {
        this.parent = parent;
        this.highlightId = highlightId;
        this.selectedId = highlightId == null ? "" : highlightId;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buildings = new ArrayList<BuildingInfo>(ModSimClientLoader.buildingInfos);
        this.ensureHighlightVisible();
        this.showPage();
        this.requestList(false);
    }

    /** 服务端列表到达后由 ModSimClientLoader 调用。 */
    public void refreshList() {
        this.buildings = new ArrayList<BuildingInfo>(ModSimClientLoader.buildingInfos);
        this.ensureHighlightVisible();
        this.showPage();
    }

    /** 从控制箱打开时，把当前建筑所在页翻出来。 */
    private void ensureHighlightVisible() {
        if (this.highlightId == null || this.highlightId.isEmpty()) {
            return;
        }
        int rows = Math.max(1, this.visibleRows());
        for (int i = 0; i < this.buildings.size(); i++) {
            BuildingInfo info = this.buildings.get(i);
            if (info != null && this.highlightId.equals(info.id)) {
                this.offset = (i / rows) * rows;
                return;
            }
        }
    }

    private void requestList(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now - this.lastRequestTime < 1200L) {
            return;
        }
        this.lastRequestTime = now;
        NetWorkLoader.net.sendToServer(new PacketRequestBuildingList());
    }

    private int panelW() {
        return Math.min(460, this.width - 16);
    }

    private int panelH() {
        return Math.min(300, this.height - 16);
    }

    private int left() {
        return GuiLayout.centerX(this.width, this.panelW());
    }

    private int top() {
        return GuiLayout.centerY(this.height, this.panelH());
    }

    private int listTop() {
        return this.top() + 48;
    }

    private int visibleRows() {
        return Math.min(ROWS_PER_PAGE, Math.max(1, (this.panelH() - 96) / ROW_H));
    }

    private void showPage() {
        this.buttonList.clear();
        int px = this.left();
        int py = this.top();
        int pw = this.panelW();
        int ph = this.panelH();

        int maxOffset = Math.max(0, this.buildings.size() - this.visibleRows());
        if (this.offset < 0) {
            this.offset = 0;
        }
        if (this.offset > maxOffset) {
            this.offset = maxOffset;
        }

        this.buttonList.add(new GuiButton(0, px + 8, py + ph - 26, 80, 20,
                t("container.sim.teleport.back")));
        GuiButton teleport = new GuiButton(1, px + pw - 88, py + ph - 26, 80, 20,
                t("container.sim.teleport.button"));
        teleport.enabled = this.selectedInfo() != null;
        this.buttonList.add(teleport);

        GuiButton prev = new GuiButton(2, px + 96, py + ph - 26, 44, 20, "<");
        prev.enabled = this.offset > 0;
        this.buttonList.add(prev);
        GuiButton next = new GuiButton(3, px + pw - 140, py + ph - 26, 44, 20, ">");
        next.enabled = this.offset + this.visibleRows() < this.buildings.size();
        this.buttonList.add(next);
    }

    private BuildingInfo selectedInfo() {
        if (this.selectedId == null || this.selectedId.isEmpty()) {
            return null;
        }
        for (BuildingInfo info : this.buildings) {
            if (info != null && this.selectedId.equals(info.id)) {
                return info;
            }
        }
        return null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int px = this.left();
        int py = this.top();
        int pw = this.panelW();
        int ph = this.panelH();
        GuiStyle.panel(px, py, pw, ph, t("container.sim.teleport.title"), this.fontRenderer);

        int x0 = px + 8;
        int innerW = pw - 16;
        int gap = 6;
        int nameW = Math.max(90, innerW * 34 / 100);
        int typeW = Math.max(60, innerW * 22 / 100);
        int coordW = Math.max(76, innerW * 25 / 100);
        int distW = Math.max(44, innerW - nameW - typeW - coordW - gap * 3);
        int x1 = x0 + nameW + gap;
        int x2 = x1 + typeW + gap;
        int x3 = x2 + coordW + gap;

        GuiStyle.inset(x0, py + 24, innerW, 18);
        GuiStyle.text(this.fontRenderer, x0 + 4, py + 29, t("container.sim.teleport.name"), GuiStyle.TEXT_MUTED);
        GuiStyle.text(this.fontRenderer, x1 + 4, py + 29, t("container.sim.teleport.type"), GuiStyle.TEXT_MUTED);
        GuiStyle.text(this.fontRenderer, x2 + 4, py + 29, t("container.sim.teleport.coords"), GuiStyle.TEXT_MUTED);
        GuiStyle.textRight(this.fontRenderer, x3 + distW - 4, py + 29, t("container.sim.teleport.distance"), GuiStyle.TEXT_MUTED);

        if (this.buildings.isEmpty()) {
            GuiStyle.text(this.fontRenderer, px + pw / 2 - this.fontRenderer.getStringWidth(t("container.sim.teleport.empty")) / 2,
                    py + 100, t("container.sim.teleport.empty"), GuiStyle.TEXT_MUTED);
        } else {
            final int top = this.listTop();
            final int rows = this.visibleRows();
            final int finalNameW = nameW;
            final int finalTypeW = typeW;
            final int finalCoordW = coordW;
            GuiLayout.scissor(x0, top, innerW, rows * ROW_H, () -> {
                for (int i = 0; i < rows; i++) {
                    int index = this.offset + i;
                    if (index >= this.buildings.size()) {
                        break;
                    }
                    BuildingInfo info = this.buildings.get(index);
                    if (info == null) {
                        continue;
                    }
                    int y = top + i * ROW_H;
                    boolean hover = mouseX >= x0 && mouseX <= x0 + innerW && mouseY >= y && mouseY < y + ROW_H;
                    boolean selected = info.id != null && info.id.equals(this.selectedId);
                    if (hover || selected) {
                        GuiStyle.hoverRow(x0, y, innerW, ROW_H);
                    } else {
                        GuiStyle.row(x0, y, innerW, ROW_H, i % 2 == 1);
                    }
                    String name = info.name == null ? "" : info.name;
                    if (info.id != null && info.id.equals(this.highlightId)) {
                        name = name + " · " + t("container.sim.teleport.current");
                    }
                    GuiStyle.text(this.fontRenderer, x0 + 4, y + 7,
                            GuiLayout.truncate(this.fontRenderer, name, finalNameW - 8), selected ? GuiStyle.GOLD : GuiStyle.TEXT);
                    GuiStyle.text(this.fontRenderer, x1 + 4, y + 7,
                            GuiLayout.truncate(this.fontRenderer, info.type, finalTypeW - 8), GuiStyle.TEXT_MUTED);
                    GuiStyle.text(this.fontRenderer, x2 + 4, y + 7,
                            GuiLayout.truncate(this.fontRenderer, this.coords(info), finalCoordW - 8), GuiStyle.TEXT_MUTED);
                    GuiStyle.textRight(this.fontRenderer, x3 + distW - 4, y + 7, this.distance(info), GuiStyle.TEXT_MUTED);
                }
            });
        }

        int total = this.buildings.size();
        int pages = Math.max(1, (total + this.visibleRows() - 1) / this.visibleRows());
        int page = total == 0 ? 1 : this.offset / this.visibleRows() + 1;
        String pageText = t("container.sim.teleport.page", page, pages);
        GuiStyle.textRight(this.fontRenderer, px + pw - 8, py + ph - 40, pageText, GuiStyle.TEXT_MUTED);
        GuiStyle.text(this.fontRenderer, px + 8, py + ph - 40,
                GuiLayout.truncate(this.fontRenderer, t("container.sim.teleport.hint"), pw - 150), GuiStyle.TEXT_MUTED);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String coords(BuildingInfo info) {
        return (int) Math.floor(info.x) + ", " + (int) Math.floor(info.y) + ", " + (int) Math.floor(info.z);
    }

    private String distance(BuildingInfo info) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            return "-";
        }
        if (info.dimension != mc.player.dimension) {
            return this.dimensionLabel(info.dimension);
        }
        double dx = mc.player.posX - info.x;
        double dy = mc.player.posY - info.y;
        double dz = mc.player.posZ - info.z;
        return Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz)) + "m";
    }

    private String dimensionLabel(int dimension) {
        if (dimension == 0) {
            return t("container.sim.teleport.overworld");
        }
        return t("container.sim.teleport.dimension", dimension);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int px = this.left();
            int innerW = this.panelW() - 16;
            int top = this.listTop();
            int rows = this.visibleRows();
            if (mouseX >= px + 8 && mouseX <= px + 8 + innerW
                    && mouseY >= top && mouseY < top + rows * ROW_H) {
                int row = (mouseY - top) / ROW_H;
                int index = this.offset + row;
                if (index >= 0 && index < this.buildings.size()) {
                    BuildingInfo info = this.buildings.get(index);
                    if (info != null) {
                        this.selectedId = info.id;
                        long now = System.currentTimeMillis();
                        if (index == this.lastClickIndex && now - this.lastClickTime < 320L) {
                            this.teleport(info);
                        } else {
                            this.lastClickIndex = index;
                            this.lastClickTime = now;
                        }
                        this.showPage();
                        return;
                    }
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        if (button.id == 0) {
            this.back();
            return;
        }
        if (button.id == 1) {
            BuildingInfo info = this.selectedInfo();
            if (info != null) {
                this.teleport(info);
            }
            return;
        }
        if (button.id == 2) {
            this.offset -= this.visibleRows();
            this.showPage();
            return;
        }
        if (button.id == 3) {
            this.offset += this.visibleRows();
            this.showPage();
        }
    }

    private void teleport(BuildingInfo info) {
        if (info == null || info.id == null || info.id.isEmpty()) {
            return;
        }
        NetWorkLoader.net.sendToServer(new PacketTeleportToBuilding(info.id));
        this.mc.currentScreen = null;
        this.mc.setIngameFocus();
    }

    private void back() {
        if (this.parent != null) {
            this.mc.displayGuiScreen(this.parent);
        } else {
            this.mc.currentScreen = null;
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        this.offset += wheel > 0 ? -1 : 1;
        this.showPage();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.back();
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            BuildingInfo info = this.selectedInfo();
            if (info != null) {
                this.teleport(info);
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static String t(String key, Object... args) {
        return new TextComponentTranslation(key, args).getUnformattedText();
    }
}
