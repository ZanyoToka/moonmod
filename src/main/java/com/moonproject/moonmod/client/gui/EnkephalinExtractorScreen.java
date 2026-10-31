package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.EnkephalinExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * 脑啡肽提取终端控制台 UI:
 * 具备催化剂注入与脑啡肽浓缩产出回路、实时提取进度条与邻接收容室监测。
 */
public class EnkephalinExtractorScreen extends AbstractContainerScreen<EnkephalinExtractorMenu> {

    public EnkephalinExtractorScreen(EnkephalinExtractorMenu menu, Inventory playerInventory, Component title) {
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

        // 1. 渲染主科技面板
        int accent = this.menu.isWorking() ? 0x00FF88 : (this.menu.hasAdjacent() ? 0x00E5FF : 0xFF3344);
        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, accent, "LC-EXTRACT-02");

        // 2. 槽位框渲染
        MoonGuiHelper.renderSlotFrame(graphics, x + 43, y + 37, 0x00E5FF | 0xFF000000); // 催化剂槽
        MoonGuiHelper.renderSlotFrame(graphics, x + 115, y + 37, 0x00FF88 | 0xFF000000); // 产物槽

        // 3. 提取转化能量流线 (x=66..110, y=44, 高 6)
        int progress = this.menu.getProgress();
        int maxProgress = Math.max(1, this.menu.getMaxProgress());
        float ratio = (float) progress / maxProgress;
        MoonGuiHelper.renderGauge(graphics, x + 66, y + 43, 44, 6, ratio, 0xFF00E5FF, 0x60101824, 0x803E5066);

        // 4. 背包槽位框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x3DF1F8, false);

        // 对接状态
        if (this.menu.hasAdjacent()) {
            graphics.drawString(this.font, "收容室: 已对接", 8, 22, 0x00FF88, false);
        } else {
            graphics.drawString(this.font, "收容室: 未检测到邻接单元", 8, 22, 0xFF4455, false);
        }

        // 工作状态
        if (this.menu.isWorking()) {
            int pct = (int) ((float) this.menu.getProgress() / Math.max(1, this.menu.getMaxProgress()) * 100);
            graphics.drawString(this.font, "提取中: " + pct + "%", 110, 22, 0x00E5FF, false);
        } else {
            graphics.drawString(this.font, "待机中", 120, 22, 0x7E8C9F, false);
        }

        // 槽位标签
        graphics.drawString(this.font, "催化剂", 40, 58, 0x8E9BAE, false);
        graphics.drawString(this.font, "脑啡肽", 112, 58, 0x8E9BAE, false);

        // 背包文字
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x728296, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;

        // 催化剂槽提示
        if (mouseX >= x + 43 && mouseX <= x + 61 && mouseY >= y + 37 && mouseY <= y + 55 && !this.menu.getSlot(0).hasItem()) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("§b催化剂槽位"));
            tooltip.add(Component.literal("§7支持投入: §f红石粉 / 青金石 / 萤石粉"));
            tooltip.add(Component.literal("§7用于激发异想体核心产生纯净脑啡肽能源。"));
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }
}
