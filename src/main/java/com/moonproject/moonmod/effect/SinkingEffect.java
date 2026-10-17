package com.moonproject.moonmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 沉沦（Sinking）：原著「下 N 次被攻击时，额外受到 X 点固定理智伤害；
 * 没有理智值的目标则受到忧郁属性伤害」。
 * <p>MC 的 MobEffect 只有一个数值，这里 amplifier + 1 = 强度（每次触发量），
 * 触发逻辑见 {@code SinkingEvents}。</p>
 */
public class SinkingEffect extends MobEffect {
    public SinkingEffect() {
        super(MobEffectCategory.HARMFUL, 0x3A5BA0);
    }

    /** 强度 = amplifier + 1。 */
    public static int potency(LivingEntity entity, MobEffect effect) {
        return entity.getEffect(effect).getAmplifier() + 1;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}