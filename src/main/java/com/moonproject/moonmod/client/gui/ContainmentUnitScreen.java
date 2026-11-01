package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.config.MoonProjectConfig;
import com.moonproject.moonmod.gui.ContainmentUnitMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * 异想体收容单元控制台 UI:
 * 具备逆卡巴拉安定度仪表、四周防爆加成读数、核心槽位与紧急抑制按键。
 */
public class ContainmentUnitScreen extends AbstractContainerScreen<ContainmentUnitMenu> {

    public ContainmentUnitScreen(ContainmentUnitMenu menu, Inventory playerInventory, Component title) {
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

        // 紧急逆卡巴拉抑制按钮
        this.addRenderableWidget(new MoonTechButton(
                this.leftPos + 104,
                this.topPos + 40,
                64,
                20,
                Component.literal("逆卡巴拉抑制"),
                btn -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                },
                this.font,
                0x00E5FF
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 1. 渲染主科技底板
        int accent = this.menu.isAlert() ? 0xFF2233 : 0x00E5FF;
        MoonGuiHelper.renderTechPanel(graphics, x, y, this.imageWidth, this.imageHeight, accent, "LC-UNIT-01");

        // 2. 核心槽位背景框 (x=79, y=41)
        MoonGuiHelper.renderSlotFrame(graphics, x + 79, y + 41, accent | 0xFF000000);

        // 3. 逆卡巴拉安定度仪表条 (长 160, 高 8)
        int counter = this.menu.getQlipothCounter();
        int max = Math.max(1, this.menu.getMaxQlipoth());
        float ratio = (float) counter / max;
        int gaugeColor = ratio > 0.6F ? 0xFF00FF88 : (ratio > 0.3F ? 0xFFFFBB00 : 0xFFFF2233);
        MoonGuiHelper.renderGauge(graphics, x + 8, y + 22, 160, 8, ratio, gaugeColor, 0x6010141A, 0x803A4454);

        // 4. 玩家背包与快捷栏框
        MoonGuiHelper.renderInventorySlotFrames(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题
        int titleColor = this.menu.isAlert() ? 0xFF5555 : 0x3DF1F8;
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, titleColor, false);

        // 安定度读数
        int counter = this.menu.getQlipothCounter();
        String counterText = "安定度: " + counter + " / 100";
        int gaugeTextColor = counter > 60 ? 0x00FF88 : (counter > 30 ? 0xFFCC00 : 0xFF3333);
        graphics.drawString(this.font, counterText, 8, 32, gaugeTextColor, false);

        // 防爆壁加成
        int platings = this.menu.getAdjacentPlatings();
        String platingText = "防爆壁加成: " + platings + "/6";
        graphics.drawString(this.font, platingText, 8, 56, 0x8E9BAE, false);

        // 当前收容对象（核心身份决定破门后放出的异想体）
        String contained = "收容对象: " + this.menu.getAbnormalityName();
        graphics.drawString(this.font, contained, 8, 44, 0x8E9BAE, false);

        // 背包文字
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x728296, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        // 仪表条 Tooltip
        int x = this.leftPos;
        int y = this.topPos;
        if (mouseX >= x + 8 && mouseX <= x + 168 && mouseY >= y + 20 && mouseY <= y + 30) {
            List<Component> list = new ArrayList<>();
            list.add(Component.literal("§b逆卡巴拉计数器 (Qlipoth Counter)"));
            list.add(Component.literal("§7当前数值: §f" + this.menu.getQlipothCounter() + " §7/ §f100"));
            list.add(Component.literal("§7当计数归零时将触发§c【收容失效】§7并释放暴走异想体！"));
            graphics.renderComponentTooltip(this.font, list, mouseX, mouseY);
        }

        // 抑制按钮 Tooltip：显示当前代价（数值来自 MoonProjectConfig）
        if (mouseX >= x + 104 && mouseX <= x + 168 && mouseY >= y + 40 && mouseY <= y + 60) {
            List<Component> list = new ArrayList<>();
            list.add(Component.literal("§b逆卡巴拉抑制"));
            list.add(Component.literal("§7恢复安定度: §a+" + MoonProjectConfig.containmentSuppressRestore()));
            list.add(Component.literal("§7代价（二选一，优先扣经验）:"));
            list.add(Component.literal("§7  - §e" + MoonProjectConfig.containmentSuppressXpLevels() + " 级经验"));
            list.add(Component.literal("§7  - §b" + MoonProjectConfig.containmentSuppressSanityCost() + " 点理智"));
            graphics.renderComponentTooltip(this.font, list, mouseX, mouseY);
        }
    }
}
