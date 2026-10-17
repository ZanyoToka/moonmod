package com.moonproject.moonmod.effect;

import com.moonproject.moonmod.config.MoonProjectConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 焦黑斩痕（设计规格书 6.2）。
 *
 * <p>本类只负责"持续掉血"这一层；另外两层分别在 {@code CombatEvents} 里实现：</p>
 * <ul>
 *   <li>无法愈合：带此效果时拦截治疗回复；</li>
 *   <li>复发：8 格内只要存在解放态的阿赖耶识，斩痕就会被重新施加；</li>
 *   <li>存在抹除：带此效果死亡时掉落物被清空。</li>
 * </ul>
 */
public class BlackScarEffect extends MobEffect {
    /** 图标/粒子颜色：设计规格书 4.2 的"焦褐黑"。 */
    public static final int COLOR = 0x2A1D18;

    public BlackScarEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 层数越高，每次结算越痛（amplifier 即"斩痕层数"，参考《边狱巴士》的火伤/出血层数）
        int damage = MoonProjectConfig.scarDamage() * (amplifier + 1);
        if (damage > 0) {
            // 用 magic 伤害源：这部分是"存在被削去"，不吃护甲
            entity.hurt(entity.damageSources().magic(), damage);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = MoonProjectConfig.scarIntervalTicks();
        return interval > 0 && duration % interval == 0;
    }
}
