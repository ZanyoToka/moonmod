package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.effect.ModEffects;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 沉沦（Sinking）触发：每被命中一次，受到「强度」点固定理智伤害；
 * 没有理智值的目标改为受到忧郁（魔法）属性伤害。
 * <p>原著里沉沦是「强度 + 层数」双数值：amplifier + 1 = 强度，
 * 层数存在实体的持久化数据 {@link #TAG_LAYERS} 里，每触发一次 -1，归零移除效果。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class SinkingEvents {
    /** 沉沦剩余层数（NBT）。 */
    public static final String TAG_LAYERS = "MoonSinkingLayers";

    /** 防止沉沦伤害触发自身递归。 */
    private static boolean resolving = false;

    /** 施加沉沦：potency 为强度，layers 为触发次数。 */
    public static void applySinking(LivingEntity target, int potency, int layers, int durationTicks) {
        target.addEffect(new MobEffectInstance(ModEffects.SINKING.get(), durationTicks, Math.max(0, potency - 1)));
        target.getPersistentData().putInt(TAG_LAYERS, Math.max(1, layers));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (resolving) {
            return;
        }
        LivingEntity target = event.getEntity();
        MobEffectInstance sinking = target.getEffect(ModEffects.SINKING.get());
        if (sinking == null) {
            return;
        }
        int potency = sinking.getAmplifier() + 1;

        CompoundTag data = target.getPersistentData();
        int layers = data.contains(TAG_LAYERS) ? data.getInt(TAG_LAYERS) : 1;

        resolving = true;
        try {
            if (Sanity.hasSanity(target)) {
                // 有理智值（玩家 / 人类型怪物）：扣理智（阿鼻叫唤时自动转 3 倍体力伤害）
                Sanity.damage(target, potency);
            } else {
                // 无理智值（蜘蛛、苦力怕、异想体等）：受到忧郁属性伤害
                target.hurt(target.damageSources().magic(), potency);
            }
        } finally {
            resolving = false;
        }

        // 层数 -1，归零则移除沉沦
        if (layers <= 1) {
            target.removeEffect(ModEffects.SINKING.get());
            data.remove(TAG_LAYERS);
        } else {
            data.putInt(TAG_LAYERS, layers - 1);
        }
    }

    private SinkingEvents() {
    }
}