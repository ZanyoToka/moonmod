package com.moonproject.moonmod.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 月亮计划 UI 专属渲染辅助工具：
 * 提供都市工坊与脑叶收容所风格的半透明黑钛面板、发光仪表线、规整槽位框与动态进度条。
 */
public class MoonGuiHelper {

    /**
     * 绘制月亮计划标准科技主面板背景 (176x166 或自定义尺寸)
     */
    public static void renderTechPanel(GuiGraphics graphics, int x, int y, int width, int height, int accentColor, String facilityCode) {
        // 主背景 (深黑半透明钛金属质感)
        graphics.fill(x, y, x + width, y + height, 0xF212141A);

        // 顶栏背景条
        graphics.fill(x + 1, y + 1, x + width - 1, y + 18, 0xD01C2028);

        // 外层微光轮廓
        graphics.renderOutline(x, y, width, height, 0x902C3340);
        graphics.renderOutline(x + 1, y + 1, width - 2, height - 2, 0x40161A22);

        // 顶部高光点缀线 (带专属强调色)
        graphics.fill(x + 2, y + 1, x + 24, y + 3, accentColor | 0xFF000000);
        graphics.fill(x + width - 24, y + 1, x + width - 2, y + 3, accentColor | 0xFF000000);

        // 底部微标线
        graphics.fill(x + 6, y + height - 3, x + width - 6, y + height - 2, 0x403A4454);
    }

    /**
     * 绘制标准槽位发光边框 (18x18)
     */
    public static void renderSlotFrame(GuiGraphics graphics, int slotX, int slotY, int highlightColor) {
        graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0x800D0F14);
        graphics.renderOutline(slotX, slotY, 18, 18, highlightColor);
    }

    /**
     * 绘制玩家 3x9 背包与 1x9 快捷栏的槽位框
     */
    public static void renderInventorySlotFrames(GuiGraphics graphics, int leftPos, int topPos) {
        // 3x9 背包槽位框 (y = topPos + 84)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int sx = leftPos + 7 + col * 18;
                int sy = topPos + 83 + row * 18;
                graphics.fill(sx, sy, sx + 18, sy + 18, 0x700B0D12);
                graphics.renderOutline(sx, sy, 18, 18, 0x403A4454);
            }
        }

        // 快捷栏分隔线
        graphics.fill(leftPos + 8, topPos + 139, leftPos + 168, topPos + 140, 0x403A4454);

        // 1x9 快捷栏槽位框 (y = topPos + 142)
        for (int col = 0; col < 9; ++col) {
            int sx = leftPos + 7 + col * 18;
            int sy = topPos + 141;
            graphics.fill(sx, sy, sx + 18, sy + 18, 0x700B0D12);
            graphics.renderOutline(sx, sy, 18, 18, 0x504B576A);
        }
    }

    /**
     * 绘制动态横向仪表条 (Gauges)
     */
    public static void renderGauge(GuiGraphics graphics, int x, int y, int width, int height, float ratio, int fillColor, int emptyColor, int borderColor) {
        graphics.fill(x, y, x + width, y + height, emptyColor);
        int filledWidth = (int) (width * Math.max(0.0F, Math.min(1.0F, ratio)));
        if (filledWidth > 0) {
            graphics.fill(x, y, x + filledWidth, y + height, fillColor);
        }
        graphics.renderOutline(x, y, width, height, borderColor);
    }
}
