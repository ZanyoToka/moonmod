package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.BookbindingTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 总类层装订台 UI:
 * 暗夜图书馆古籍整理工作台，具备金箔纸屑装订槽与优雅的典籍整理风格。
 */
public class BookbindingTableScreen extends AbstractContainerScreen<BookbindingTableMenu> {

    public BookbindingTableScreen(BookbindingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 5;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 1. 渲染废墟图书馆风格暗金古籍面板
        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, 0xD4AF37, "LIBRARY-KETER-04");

        // 2. 3x3 合成格背景框 (x=29, y=16)
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                MoonGuiHelper.renderSlotFrame(graphics, x + 29 + c * 18, y + 16 + r * 18, 0x80D4AF37);
            }
        }

        // 3. 产物箭头与产物槽 (x=123, y=34)
        graphics.fill(x + 90, y + 39, x + 114, y + 41, 0xFFD4AF37);
        MoonGuiHelper.renderSlotFrame(graphics, x + 123, y + 34, 0xFFFFD700);

        // 4. 玩家背包槽位框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题 (优雅金)
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFD700, false);
        // 背包文字
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x9B8B70, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
