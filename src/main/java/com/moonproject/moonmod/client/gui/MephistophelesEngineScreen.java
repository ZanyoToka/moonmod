package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.gui.MephistophelesEngineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * 梅菲斯特引擎核心控制台 UI:
 * 具备黑红工业生物柴油引擎风格、心跳脉冲与客席理智协同光环仪表。
 */
public class MephistophelesEngineScreen extends AbstractContainerScreen<MephistophelesEngineMenu> {

    public MephistophelesEngineScreen(MephistophelesEngineMenu menu, Inventory playerInventory, Component title) {
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

        int accent = this.menu.isLit() ? 0xFF3322 : 0x884433;
        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, accent, "BUS-MEPHISTO-07");

        // 燃料槽框 (x=79, y=37)
        MoonGuiHelper.renderSlotFrame(graphics, x + 79, y + 37, 0xFFFF4422);

        // 燃烧能量仪表条 (长 120, 高 6)
        int fuel = this.menu.getFuelTicks();
        int max = Math.max(1, this.menu.getMaxFuelTicks());
        float ratio = (float) fuel / max;
        MoonGuiHelper.renderGauge(graphics, x + 28, y + 58, 120, 6, ratio, 0xFFFF3322, 0x60301010, 0x80662211);

        // 背包槽位框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题 (猩红色)
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF5544, false);

        // 燃烧状态
        if (this.menu.isLit()) {
            int sec = this.menu.getFuelTicks() / 20;
            graphics.drawString(this.font, "§c引擎运转中: §f" + sec + "秒", 8, 22, 0xFF5544, false);
        } else {
            graphics.drawString(this.font, "引擎熄火: 待喂食生化燃料", 8, 22, 0x887777, false);
        }

        // 客席协同
        int seats = this.menu.getConnectedSeats();
        graphics.drawString(this.font, "协同客席: " + seats + " 座", 112, 22, 0x00FF88, false);

        // 背包文字
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x8E807D, false);
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
            list.add(Component.literal("§c生化燃料投料槽"));
            list.add(Component.literal("§7支持喂食: §f腐肉、骨头、蜘蛛眼、生物掉落物"));
            list.add(Component.literal("§7运转时大幅强化周围车厢客席的回理智与自愈速率！"));
            graphics.renderComponentTooltip(this.font, list, mouseX, mouseY);
        }
    }
}
