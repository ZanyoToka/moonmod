package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 硬币结果 HUD：画在理智圆环正下方，用图标显示正/负币（不用文字）。 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, value = Dist.CLIENT)
public final class CoinHudOverlay {
    private static final ResourceLocation HEADS =
            ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "textures/gui/coin_heads.png");
    private static final ResourceLocation TAILS =
            ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "textures/gui/coin_tails.png");

    private static final int COIN = 12;
    private static final int GAP = 1;
    /** 与 SanityHudOverlay 保持一致：圆环 22px，位于准星左侧偏下。 */
    private static final int SANITY_SIZE = 22;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !CoinClient.visible()) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        // 与理智圆环同列（左上角），放在它正下方
        int baseX = 6;
        int baseY = 6 + SANITY_SIZE + 2;

        int count = Math.min(CoinClient.count(), 8);
        int totalWidth = count * COIN + (count - 1) * GAP;
        int startX = baseX + (SANITY_SIZE - totalWidth) / 2;

        for (int i = 0; i < count; i++) {
            ResourceLocation texture = CoinClient.isHeads(i) ? HEADS : TAILS;
            graphics.blit(texture, startX + i * (COIN + GAP), baseY, 0, 0, COIN, COIN, COIN, COIN);
        }

        String mult = String.format("x%.2f", CoinClient.getMultiplier());
        graphics.drawCenteredString(minecraft.font, mult,
                baseX + SANITY_SIZE / 2, baseY + COIN + 1, 0xFFFFFFFF);
    }

    private CoinHudOverlay() {
    }
}
