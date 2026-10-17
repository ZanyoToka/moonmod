package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 理智值 HUD：准星右侧的圆形图标 + 中央数字。
 * <p>负数用红色圆环、正数用蓝色圆环（与原著一致）。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, value = Dist.CLIENT)
public final class SanityHudOverlay {
    private static final ResourceLocation RING_RED =
            ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "textures/gui/sanity_red.png");
    private static final ResourceLocation RING_BLUE =
            ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "textures/gui/sanity_blue.png");

    /** 圆环显示尺寸（贴图 32x32）。 */
    private static final int SIZE = 22;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int sanity = SanityClient.get();

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        // 准星右侧偏下，避免挡住准星与准星 HUD 的目标状态列表
        // 屏幕左上角
        int x = 6;
        int y = 6;

        graphics.blit(sanity < 0 ? RING_RED : RING_BLUE, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);

        String text = String.valueOf(sanity);
        int color = sanity < 0 ? 0xFFFF8080 : 0xFFFFFFFF;
        int textX = x + SIZE / 2 - minecraft.font.width(text) / 2;
        int textY = y + SIZE / 2 - 4;
        graphics.drawString(minecraft.font, text, textX + 1, textY + 1, 0x80000000, false);
        graphics.drawString(minecraft.font, text, textX, textY, color, false);
    }

    private SanityHudOverlay() {
    }
}
