package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.GoldenBoughShrineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * 黄金枝基座 / 光之种发芽坛控制台 UI:
 * 华丽金白圣物坛，插枝直冲天际光柱，显示绝对光之领域免受恐慌庇护与异想体镇定。
 */
public class GoldenBoughShrineScreen extends AbstractContainerScreen<GoldenBoughShrineMenu> {

    public GoldenBoughShrineScreen(GoldenBoughShrineMenu menu, Inventory playerInventory, Component title) {
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

        int accent = this.menu.hasBough() ? 0xFFD700 : 0xAA9955;
        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, accent, "SEED-OF-LIGHT-09");

        // 黄金枝槽位 (x=79, y=37)
        MoonGuiHelper.renderSlotFrame(graphics, x + 79, y + 37, 0xFFFFD700);

        // 领域共振光环指示条
        float ratio = this.menu.hasBough() ? 1.0F : 0.0F;
        MoonGuiHelper.renderGauge(graphics, x + 28, y + 58, 120, 6, ratio, 0xFFFFE066, 0x60262210, 0x80665518);

        // 背包槽位框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题 (神圣亮金)
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFE066, false);

        // 领域状态
        if (this.menu.hasBough()) {
            graphics.drawString(this.font, "§e[光之领域已激发] 半径 48 格庇护", 8, 22, 0xFFFFD700, false);
            graphics.drawString(this.font, "人类理智锁定拉满 | 异想体全境镇定", 8, 30, 0x00FF88, false);
        } else {
            graphics.drawString(this.font, "基座待机: 请供奉【黄金枝】或圣物", 8, 22, 0xAA9E7A, false);
        }

        // 背包文字
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x968B72, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;

        if (mouseX >= x + 79 && mouseX <= x + 97 && mouseY >= y + 37 && mouseY <= y + 55 && !this.menu.getSlot(0).hasItem()) {
            List<Component> list = new ArrayList<>();
            list.add(Component.literal("§e黄金枝供奉位"));
            list.add(Component.literal("§7支持放入: §f黄金枝、下界之星、金苹果、E.G.O核心"));
            list.add(Component.literal("§7供奉后激发直冲天际的神圣光柱，构筑不可侵犯的48格绝对光之领域！"));
            graphics.renderComponentTooltip(this.font, list, mouseX, mouseY);
        }
    }
}
