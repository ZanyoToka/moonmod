package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.ReinforcedPlatingMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 都市重装防爆壁监控 UI:
 * 坚实冷铁防爆面板、IV级都市巢装甲标准、抗爆系数与邻接收容室抑制共振分析。
 */
public class ReinforcedPlatingScreen extends AbstractContainerScreen<ReinforcedPlatingMenu> {

    public ReinforcedPlatingScreen(ReinforcedPlatingMenu menu, Inventory playerInventory, Component title) {
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

        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, 0x5C6B7F, "CITY-ARMOR-10");

        // 护壁装甲抗性仪表
        graphics.fill(x + 12, y + 20, x + 164, y + 46, 0x600F1318);
        graphics.renderOutline(x + 12, y + 20, 152, 26, 0x805C6B7F);

        // 结构强度满格进度条
        MoonGuiHelper.renderGauge(graphics, x + 16, y + 40, 144, 4, 1.0F, 0xFF4FD6BE, 0x401B242E, 0x802B3746);

        // 背包槽位框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x8E9BAE, false);

        graphics.drawString(this.font, "结构完整度: 100% (抗爆: 1200)", 16, 24, 0x4FD6BE, false);
        graphics.drawString(this.font, "标准: 都市巢级特种防御合金", 16, 32, 0x7E8C9F, false);

        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x6C7786, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
