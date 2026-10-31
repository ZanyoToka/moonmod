package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.TrinketMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 饰品界面（按 T 打开）。 */
public class TrinketScreen extends AbstractContainerScreen<TrinketMenu> {

    public TrinketScreen(TrinketMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 182;
        this.inventoryLabelY = 88;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 纯色底板（不依赖贴图）
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFC6C6C6);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 3, 0xFF555555);
        graphics.fill(leftPos, topPos + imageHeight - 3, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);
        graphics.fill(leftPos, topPos, leftPos + 3, topPos + imageHeight, 0xFF555555);
        graphics.fill(leftPos + imageWidth - 3, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);
        // 四个饰品槽的底 + 标签
        int[] xs = {44, 80, 116, 152};
        String[] labels = {"头饰", "项链", "戒指", "戒指"};
        for (int i = 0; i < 4; i++) {
            graphics.fill(leftPos + xs[i] - 1, topPos + 39, leftPos + xs[i] + 17, topPos + 57, 0xFF8B8B8B);
            graphics.fill(leftPos + xs[i], topPos + 40, leftPos + xs[i] + 16, topPos + 56, 0xFF373737);
            int w = this.font.width(labels[i]);
            graphics.drawString(this.font, labels[i], leftPos + xs[i] + 8 - w / 2, topPos + 24, 0xFF404040, false);
        }
        // 标题
        graphics.drawString(this.font, "§6异想体饰品", leftPos + 8, topPos + 10, 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}