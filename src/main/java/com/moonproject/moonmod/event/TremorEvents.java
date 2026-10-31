package com.moonproject.moonmod.event;

import com.moonproject.moonmod.effect.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 震颤（Tremor）机制：
 * 叠满 {@link #EXPLODE_STACKS} 层时，在原处引发一次不破坏方块的爆炸，
 * 伤害等于「武器白值」（攻击者的基础攻击力）。
 */
public final class TremorEvents {

    /** 爆炸所需层数。 */
    public static final int EXPLODE_STACKS = 10;
    /** 爆炸半径（不破坏方块，仅做伤害与击退）。 */
    public static final float EXPLODE_RADIUS = 3.0F;

    /**
     * 给目标叠加震颤层数；达到 10 层时触发爆炸并清空层数。
     *
     * @param target   被叠层的目标
     * @param stacks   本次叠加的层数
     * @param attacker 施加者（用于取武器白值与伤害归属，可为 null）
     * @return 是否触发了爆炸
     */
    public static boolean addTremor(LivingEntity target, int stacks, LivingEntity attacker) {
        if (target == null || stacks <= 0) {
            return false;
        }
        MobEffect effect = ModEffects.TREMOR.get();
        MobEffectInstance current = target.getEffect(effect);
        int now = current == null ? 0 : current.getAmplifier() + 1;
        int next = now + stacks;

        if (next >= EXPLODE_STACKS) {
            // 达到阈值：先移除层数，再爆炸
            target.removeEffect(effect);
            explode(target, attacker);
            return true;
        }
        // 未达阈值：刷新层数（amplifier = 层数 - 1），维持 10 分钟
        target.addEffect(new MobEffectInstance(effect, 20 * 600, next - 1, false, true, true));
        return false;
    }

    /** 在目标位置引发一次不破坏方块的爆炸，伤害 = 武器白值。 */
    private static void explode(LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        float damage = weaponBaseAttack(attacker);
        Vec3 pos = target.position();

        // 不破坏方块：BlockInteraction.NONE
        server.explode(attacker, pos.x, pos.y + target.getBbHeight() * 0.5, pos.z,
                EXPLODE_RADIUS, Level.ExplosionInteraction.NONE);

        // 爆炸本身的伤害不受「武器白值」控制，这里补一次定向伤害
        if (damage > 0) {
            target.hurt(server.damageSources().magic(), damage);
        }
        if (attacker != null) {
            attacker.setLastHurtMob(target);
        }
    }

    /** 取攻击者主手武器的攻击力（白值）；没有武器则用拳头 1.0。 */
    public static float weaponBaseAttack(LivingEntity attacker) {
        if (attacker == null) {
            return 1.0F;
        }
        return (float) attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
    }

    private TremorEvents() {
    }
}