package com.moonproject.moonmod.client.gui;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.blockentity.ExpBurnerBlockEntity;
import com.moonproject.moonmod.gui.ExpBurnerMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝色烧书熔炉专属 GUI 界面：
 * 采用黑石钛金工坊面板 + 青蓝色能量管线，全定制科技按钮、规整对称的左向右能量转化流线，
 * 彻底消除原版按钮错位溢出与槽位凌乱问题。
 */
public class ExpBurnerScreen extends AbstractContainerScreen<ExpBurnerMenu> {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "textures/gui/container/exp_burner.png");

    private Button extractButton;

    public ExpBurnerScreen(ExpBurnerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 10;
        this.titleLabelY = 9;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 70;
    }

    @Override
    protected void init() {
        super.init();

        // 定制专属科技风格“提取”按钮：精准位于 (x + 122, y + 34)，尺寸 42x20，不压边框
        this.extractButton = new TechButton(
                this.leftPos + 122,
                this.topPos + 34,
                42,
                20,
                Component.literal("提取"),
                btn -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                },
                this.font
        );
        this.extractButton.setTooltip(Tooltip.create(Component.translatable("gui.moonproject.exp_burner.extract_tooltip")));

        this.addRenderableWidget(this.extractButton);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 1. 渲染主面板背景 (176x166)
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 2. 燃烧中时：渲染青蓝灵魂火焰 (14x14)，位于 x + 57, y + 37
        if (this.menu.isLit()) {
            int flameHeight = this.menu.getLitProgressScaled(14);
            graphics.blit(TEXTURE, x + 57, y + 37 + (14 - flameHeight), 176, 14 - flameHeight, 14, flameHeight);

            // 3. 正在燃烧时：渲染流动能量箭头 (16x15)，位于 x + 75, y + 36
            graphics.blit(TEXTURE, x + 75, y + 36, 176, 16, 16, 15);
        }

        // 4. 经验流体柱 (14x46)，位于 x + 98, y + 21
        int fluidHeight = this.menu.getExpProgressScaled(46);
        if (fluidHeight > 0) {
            graphics.blit(TEXTURE, x + 98, y + 21 + (46 - fluidHeight), 176, 32 + (46 - fluidHeight), 14, fluidHeight);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 标题：青蓝色发光文本
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x3DF1F8, false);
        // 背包标题：暗银灰色，位于分割线上方
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x8E9BAE, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        // 自定义经验柱与输入槽悬停 Tooltip
        renderCustomTooltips(graphics, mouseX, mouseY);
    }

    private void renderCustomTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 1. 鼠标悬停在经验柱 (96..114, 19..69)
        if (mouseX >= x + 96 && mouseX <= x + 114 && mouseY >= y + 19 && mouseY <= y + 69) {
            List<Component> tooltip = new ArrayList<>();
            int stored = this.menu.getStoredExp();
            tooltip.add(Component.translatable("gui.moonproject.exp_burner.stored", stored, ExpBurnerBlockEntity.MAX_EXP_CAPACITY));

            if (this.menu.isLit()) {
                tooltip.add(Component.translatable("gui.moonproject.exp_burner.rate", this.menu.getExpRate()));
            } else {
                tooltip.add(Component.translatable("gui.moonproject.exp_burner.idle"));
            }
            tooltip.add(Component.translatable("gui.moonproject.exp_burner.extract_hint"));
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }

        // 2. 鼠标悬停在输入槽 (35..52, 34..51) 且槽内无物品时
        if (mouseX >= x + 35 && mouseX <= x + 52 && mouseY >= y + 34 && mouseY <= y + 51) {
            if (!this.menu.getSlot(0).hasItem()) {
                List<Component> fuelHint = new ArrayList<>();
                fuelHint.add(Component.translatable("gui.moonproject.exp_burner.fuel_title"));
                fuelHint.add(Component.translatable("gui.moonproject.exp_burner.fuel_desc1"));
                fuelHint.add(Component.translatable("gui.moonproject.exp_burner.fuel_desc2"));
                graphics.renderComponentTooltip(this.font, fuelHint, mouseX, mouseY);
            }
        }
    }

    /**
     * 定制科技风格按钮：
     * 采用深钛金属质感底板 + 青蓝发光边框，悬停时高亮流光，与暗黑工坊主题深度统一。
     */
    private static class TechButton extends Button {
        private final Font font;

        public TechButton(int x, int y, int width, int height, Component message, OnPress onPress, Font font) {
            super(Button.builder(message, onPress).bounds(x, y, width, height));
            this.font = font;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // 根据状态选择贴图切片 (默认: 80, 悬停: 102, 禁用/按下: 124)
            int vOffset = 80;
            if (!this.active) {
                vOffset = 124;
            } else if (this.isHoveredOrFocused()) {
                vOffset = 102;
            }

            // 绘制定制按钮贴图 (42x20)
            graphics.blit(TEXTURE, this.getX(), this.getY(), 176, vOffset, this.width, this.height);

            // 绘制居中发光文字
            int textColor = !this.active ? 0x6E7B8B : (this.isHoveredOrFocused() ? 0xFFFFFF : 0x5CE1E6);
            int textX = this.getX() + (this.width - this.font.width(this.getMessage())) / 2;
            int textY = this.getY() + (this.height - 8) / 2;
            graphics.drawString(this.font, this.getMessage(), textX, textY, textColor, false);
        }
    }
}
