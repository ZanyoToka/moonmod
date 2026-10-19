package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.ResonanceCollectorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 场谐振能源采集单元的界面。
 *
 * <p>四个工作按钮画在底图左侧的四个框上，图标用 {@code work_*.png}；
 * 底部进度条显示当前工作进度。</p>
 */
public class ResonanceCollectorScreen extends AbstractContainerScreen<ResonanceCollectorMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("moonproject", "textures/gui/resonance_gui_tall.png");

    /** 四个工作按钮的图标。 */
    private static final ResourceLocation[] ICONS = {
            ResourceLocation.fromNamespaceAndPath("moonproject", "textures/gui/work_oppress.png"),
            ResourceLocation.fromNamespaceAndPath("moonproject", "textures/gui/work_commune.png"),
            ResourceLocation.fromNamespaceAndPath("moonproject", "textures/gui/work_insight.png"),
            ResourceLocation.fromNamespaceAndPath("moonproject", "textures/gui/work_instinct.png"),
    };

    /** 四个按钮在界面里的位置（相对 leftPos/topPos，和底图的四个框对齐）。 */
    private static final int BTN_X = 29;
    private static final int BTN_W = 14;
    private static final int BTN_H = 14;
    private static final int[] BTN_Y = {32, 50, 67, 80};

    public ResonanceCollectorScreen(ResonanceCollectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 226;
        this.inventoryLabelY = 144;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // 四个按钮图标
        for (int i = 0; i < 4; i++) {
            int x = leftPos + BTN_X;
            int y = topPos + BTN_Y[i];
            // 悬停高亮
            boolean hover = mouseX >= x && mouseX < x + BTN_W && mouseY >= y && mouseY < y + BTN_H;
            if (hover) {
                graphics.fill(x, y, x + BTN_W, y + BTN_H, 0x40FFFFFF);
            }
            graphics.blit(ICONS[i], x, y, 0, 0, BTN_W, BTN_H, BTN_W, BTN_H);
        }
        // 工作进度：底图下方那条做成进度条 + 百分比文字
        {
            // 通过 ContainerData 读服务端同步过来的状态
            int syncedType = this.menu.getWorkType();
            int syncedProgress = this.menu.getWorkProgress();
            int total = com.moonproject.moonmod.blockentity.ResonanceCollectorBlockEntity.WORK_TOTAL;
            int done = Math.min(syncedProgress, total);
            int barX = leftPos + 36;
            int barY = topPos + 118;
            int barW = 100;
            int barH = 5;
            // 槽底
            graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF0A0E12);
            graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF1B2530);
            if (done > 0) {
                int w = (int) ((long) barW * done / total);
                // 亮青渐变（分两段模拟）
                graphics.fill(barX, barY, barX + w, barY + barH, 0xFF33D6E0);
                graphics.fill(barX, barY, barX + w, barY + 2, 0xFF7FF0F8);
            }
            // 文字：百分比 + 剩余秒数
            if (syncedType >= 0) {
                int pct = (int) ((long) done * 100 / total);
                int secLeft = (total - done) / 20;
                String txt = pct + "%  " + secLeft + "s";
                graphics.drawString(this.font, txt, leftPos + 60, topPos + 108, 0xFF7FF0F8, false);
                String name = com.moonproject.moonmod.blockentity.ResonanceCollectorBlockEntity.WORK_NAMES[Math.max(0, Math.min(3, syncedType))];
                graphics.drawString(this.font, name, leftPos + 30, topPos + 108, 0xFFFFFFFF, false);
            } else {
                graphics.drawString(this.font, "待机", leftPos + 60, topPos + 108, 0xFF8899A6, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < 4; i++) {
            int x = leftPos + BTN_X;
            int y = topPos + BTN_Y[i];
            if (mouseX >= x && mouseX < x + BTN_W && mouseY >= y && mouseY < y + BTN_H) {
                if (this.minecraft != null && this.minecraft.gameMode != null) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}