package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.MeltdownSirenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 脑叶警报广播机控制台 UI:
 * 具备声光全息警报仪态、危险收容室热力监控、手动防空鸣笛按键。
 */
public class MeltdownSirenScreen extends AbstractContainerScreen<MeltdownSirenMenu> {

    public MeltdownSirenScreen(MeltdownSirenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 5;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(new MoonTechButton(
                this.leftPos + 48,
                this.topPos + 48,
                80,
                20,
                Component.literal("手动警号切换"),
                btn -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                },
                this.font,
                0xFF2233
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        int accent = this.menu.isAlert() ? 0xFF2233 : 0x00FF88;
        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, accent, "LC-SIREN-03");

        // 中间警报指示灯雷达框
        graphics.fill(x + 20, y + 20, x + 156, y + 44, 0x5010141D);
        graphics.renderOutline(x + 20, y + 20, 136, 24, accent | 0xFF000000);

        // 背包槽位框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题
        int titleColor = this.menu.isAlert() ? 0xFF5555 : 0x3DF1F8;
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, titleColor, false);

        // 状态字样
        if (this.menu.isAlert()) {
            graphics.drawString(this.font, "§c[警报鸣响中] 32格戒备已激活", 26, 26, 0xFF3333, false);
            graphics.drawString(this.font, "危急收容室数量: " + this.menu.getNearbyMeltdowns() + " 座", 26, 34, 0xFFBB00, false);
        } else {
            graphics.drawString(this.font, "§a[待机] 全厂收容所监测正常", 26, 26, 0x00FF88, false);
            graphics.drawString(this.font, "广播监测范围: 半径 24 格", 26, 34, 0x8E9BAE, false);
        }

        // 背包文字
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x728296, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
