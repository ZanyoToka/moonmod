package com.moonproject.moonmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.IntSupplier;

/**
 * 通用「周期性伤害」效果，用来快速实现各类 DoT（燃烧等）。
 *
 * <p>参考《废墟图书馆》/《边狱巴士》的 DoT 规则：每隔一段 tick 结算一次伤害，
 * 伤害随层数（amplifier + 1）线性增长。</p>
 */
public class PeriodicDamageEffect extends MobEffect {
    private final IntSupplier damagePerStack;
    private final IntSupplier intervalTicks;

    public PeriodicDamageEffect(MobEffectCategory category, int color,
                                IntSupplier damagePerStack, IntSupplier intervalTicks) {
        super(category, color);
        this.damagePerStack = damagePerStack;
        this.intervalTicks = intervalTicks;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        int damage = damagePerStack.getAsInt() * (amplifier + 1);
        if (damage > 0) {
            entity.hurt(entity.damageSources().magic(), damage);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = intervalTicks.getAsInt();
        return interval > 0 && duration % interval == 0;
    }
}
