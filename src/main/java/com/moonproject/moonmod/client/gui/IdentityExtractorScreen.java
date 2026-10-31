package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.IdentityExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 人格抽取共鸣舱控制台 UI:
 * 苍白镜面裂痕风格面板、投入异想体碎片与书页媒介、启动镜之共鸣抽卡。
 */
public class IdentityExtractorScreen extends AbstractContainerScreen<IdentityExtractorMenu> {

    public IdentityExtractorScreen(IdentityExtractorMenu menu, Inventory playerInventory, Component title) {
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
                Component.literal("抽取"),
                btn -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                },
                this.font,
                0x9955FF
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        int accent = this.menu.isExtracting() ? 0xC855FF : 0x7744DD;
        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, accent, "LIMBUS-MIRROR-06");

        // 槽位框渲染
        MoonGuiHelper.renderSlotFrame(graphics, x + 43, y + 37, 0x807744DD);
        MoonGuiHelper.renderSlotFrame(graphics, x + 115, y + 37, 0xFFC855FF);

        // 抽取进度条 (位于按钮下方)
        int progress = this.menu.getProgress();
        int max = Math.max(1, this.menu.getMaxProgress());
        float ratio = (float) progress / max;
        MoonGuiHelper.renderGauge(graphics, x + 68, y + 58, 42, 4, ratio, 0xFFC855FF, 0x60201430, 0x80503070);

        // 背包框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题 (紫罗兰色)
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xDA88FF, false);

        // 状态字样
        if (this.menu.isExtracting()) {
            graphics.drawString(this.font, "§d镜面共鸣中...", 8, 22, 0xDA88FF, false);
        } else {
            graphics.drawString(this.font, "镜技术: 待命中", 8, 22, 0x8E80A0, false);
        }

        // 背包文字
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x7E7288, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
