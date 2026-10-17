package com.moonproject.moonmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 「层数型」状态效果：自身不做周期结算，层数（amplifier + 1）由 {@code CombatEvents}
 * 在命中时读取与消耗。
 *
 * <p>对应《边狱巴士》的呼吸 / 出血这类"层数"资源——参考「小指·地彗星 良秀」：
 * 它的各技能强度由**呼吸与火伤/出血的层数总和**决定。</p>
 */
public class StackEffect extends MobEffect {
    public StackEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
