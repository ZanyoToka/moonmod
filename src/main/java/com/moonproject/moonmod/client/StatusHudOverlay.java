package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.effect.ModEffects;
import com.moonproject.moonmod.item.AlayaVijnanaItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 状态读数 HUD（客户端）。
 *
 * <p>原版 HUD 只显示玩家自己的状态效果，看不见怪身上的层数。这里分两块显示：</p>
 * <ul>
 *   <li><b>自身</b>：手持阿赖耶识时，屏幕右下角常显 呼吸 / 斩缘 层数（这两项属于自己）；</li>
 *   <li><b>目标</b>：准星对准生物时，在其右侧列出该生物身上本模组的状态（层数 + 剩余秒数）。</li>
 * </ul>
 *
 * <p>只读客户端数据（自身效果 + 准星 {@link EntityHitResult}），不做任何网络同步。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, value = Dist.CLIENT)
public final class StatusHudOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        try {
            render(event);
        } catch (Exception ignored) {
            // 宁可少画一行，也不要因为 HUD 异常影响游戏画面
        }
    }

    private static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();

        // ── 自身：手持阿赖耶识时显示呼吸 / 斩缘 ──
        if (AlayaVijnanaItem.isDrawnBlade(minecraft.player.getMainHandItem())) {
            int y = graphics.guiHeight() - 46;
            int x = graphics.guiWidth() - 132;
            int poise = stacks(minecraft.player, ModEffects.POISE.get());
            int cutbond = stacks(minecraft.player, ModEffects.CUTBOND.get());
            if (poise > 0 || cutbond > 0) {
                graphics.drawString(minecraft.font, "呼吸 x" + poise + "   斩缘 x" + cutbond,
                        x, y, 0xD8E8F0, true);
            }
        }

        // ── 目标：准星对准的生物 ──
        if (!(minecraft.hitResult instanceof EntityHitResult hit)) {
            return;
        }
        Entity entity = hit.getEntity();
        if (!(entity instanceof LivingEntity living) || living == minecraft.player) {
            return;
        }
        // 缩小显示：整体缩放 0.7
        final float scale = 0.7F;
        int x = graphics.guiWidth() / 2 + 14;
        int y = graphics.guiHeight() / 2 - 24;
        int line = 0;
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        for (MobEffectInstance instance : living.getActiveEffects()) {
            MobEffect effect = instance.getEffect();
            if (!isTargetEffect(effect)) {
                continue;
            }
            String label = effect.getDisplayName().getString()
                    + " x" + (instance.getAmplifier() + 1);
            graphics.drawString(minecraft.font,
                    label,
                    (int) (x / scale),
                    (int) ((y + line * 7) / scale),
                    colorFor(effect), true);
            line++;
        }
        if (line > 0) {
            graphics.drawString(minecraft.font, living.getName().getString(),
                    (int) (x / scale), (int) ((y - 9) / scale), 0xFFFFFF, true);
        }
        graphics.pose().popPose();
    }

    private static int stacks(LivingEntity entity, MobEffect effect) {
        MobEffectInstance instance = entity.getEffect(effect);
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    private static boolean isTargetEffect(MobEffect effect) {
        return effect == ModEffects.BLEED.get()
                || effect == ModEffects.BURN.get()
                || effect == ModEffects.BLACK_SCAR.get()
                || effect == ModEffects.RUPTURE.get()
                || effect == ModEffects.TREMOR.get()
                || effect == ModEffects.LACERATION.get()
                || effect == ModEffects.CUTOFF.get();
    }

    private static int colorFor(MobEffect effect) {
        if (effect == ModEffects.BLEED.get()) {
            return 0xFF6B6B;
        }
        if (effect == ModEffects.BURN.get()) {
            return 0xFFA640;
        }
        if (effect == ModEffects.BLACK_SCAR.get()) {
            return 0xC0603A;
        }
        if (effect == ModEffects.RUPTURE.get()) {
            return 0xD070B0;
        }
        if (effect == ModEffects.TREMOR.get()) {
            return 0x7FA0FF;
        }
        if (effect == ModEffects.LACERATION.get()) {
            return 0xFF80A0;
        }
        return 0xB0A0FF;
    }

    private StatusHudOverlay() {
    }
}
