package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.KCorpInjectorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * K公司再生安瓿灌装机控制台 UI:
 * 象牙白机体配荧光绿纳米原液流体管线、空瓶分装与超速自愈状态。
 */
public class KCorpInjectorScreen extends AbstractContainerScreen<KCorpInjectorMenu> {

    public KCorpInjectorScreen(KCorpInjectorMenu menu, Inventory playerInventory, Component title) {
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
                this.leftPos + 68,
                this.topPos + 36,
                42,
                20,
                Component.literal("灌装"),
                btn -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                },
                this.font,
                0x00FF88
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        int accent = 0x00FF88;
        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, accent, "KCORP-SINGULARITY-08");

        // 槽位框 (x=43, y=37) 和 (x=115, y=37)
        MoonGuiHelper.renderSlotFrame(graphics, x + 43, y + 37, 0x803E5066);
        MoonGuiHelper.renderSlotFrame(graphics, x + 115, y + 37, 0xFF00FF88);

        // 储液罐计量条 (长 120, 高 5)
        MoonGuiHelper.renderGauge(graphics, x + 28, y + 58, 120, 5, 1.0F, 0xFF00E676, 0x60082014, 0x80104A2A);

        // 背包槽位框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题 (青翠绿)
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x00FF88, false);

        // 状态字样
        graphics.drawString(this.font, "奇点状态: 纳米眼泪原液饱充", 8, 22, 0x00E676, false);

        // 槽位标签
        graphics.drawString(this.font, "空瓶", 45, 58, 0x8E9BAE, false);
        graphics.drawString(this.font, "安瓿", 117, 58, 0x8E9BAE, false);

        // 背包文字
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x76887B, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
