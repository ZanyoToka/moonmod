package com.moonproject.moonmod.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * 月亮计划专属科技风格按钮：
 * 深钛黑底板 + 霓虹发光边框，悬停与点击时具备动态流光变色。
 */
public class MoonTechButton extends Button {
    private final Font font;
    private final int accentColor;

    public MoonTechButton(int x, int y, int width, int height, Component message, OnPress onPress, Font font, int accentColor) {
        super(Button.builder(message, onPress).bounds(x, y, width, height));
        this.font = font;
        this.accentColor = accentColor;
    }

    public MoonTechButton(int x, int y, int width, int height, Component message, OnPress onPress, Font font) {
        this(x, y, width, height, message, onPress, font, 0x00E5FF); // 默认青蓝
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHoveredOrFocused();

        // 1. 底板渐变填充
        int bgColor = hovered ? 0xE0222730 : 0xD014161C;
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);

        // 2. 边框渲染 (悬停时高亮)
        int borderColor = hovered ? this.accentColor | 0xFF000000 : 0x803E4857;
        graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);

        // 3. 悬停时左侧发光微标
        if (hovered) {
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + 3, this.getY() + this.height - 1, this.accentColor | 0xFF000000);
        }

        // 4. 文字居中渲染
        int textColor = !this.active ? 0x606060 : (hovered ? 0xFFFFFF : 0xC8D2DC);
        int textX = this.getX() + (this.width - this.font.width(this.getMessage())) / 2;
        int textY = this.getY() + (this.height - 8) / 2;
        graphics.drawString(this.font, this.getMessage(), textX, textY, textColor, false);
    }
}
