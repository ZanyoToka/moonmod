package com.moonproject.moonmod.event;

import com.moonproject.moonmod.effect.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 呼吸法（Poise）暴击机制：
 * <ul>
 *   <li>每层呼吸法：<b>+0.5% 暴击率</b>、<b>+5% 暴击伤害</b></li>
 *   <li>玩家造成的伤害结算时掷一次暴击；命中则伤害 × (1 + 暴伤)</li>
 *   <li>暴击时在目标位置爆出暴击粒子</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = com.moonproject.moonmod.MoonProjectMod.MODID)
public final class PoiseCritEvents {

    /** 每层呼吸法的暴击率加成。 */
    public static final double CRIT_CHANCE_PER_STACK = 0.005D;
    /** 每层呼吸法的暴击伤害加成。 */
    public static final double CRIT_DAMAGE_PER_STACK = 0.05D;
    /** 暴击率上限（避免 100% 以上无意义）。 */
    public static final double MAX_CRIT_CHANCE = 1.0D;
    /** 呼吸法层数上限（30 层封顶：+15% 暴击率、+150% 暴伤）。 */
    public static final int MAX_POISE_STACKS = 30;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        int stacks = poiseStacks(player);
        if (stacks <= 0) {
            return;
        }
        double chance = Math.min(MAX_CRIT_CHANCE, CRIT_CHANCE_PER_STACK * stacks);
        double critDamage = CRIT_DAMAGE_PER_STACK * stacks;

        if (player.getRandom().nextDouble() >= chance) {
            return;
        }
        // 暴击：伤害 × (1 + 暴伤)
        event.setAmount((float) (event.getAmount() * (1.0D + critDamage)));

        LivingEntity target = event.getEntity();
        if (target.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    12, 0.35, 0.35, 0.35, 0.15);
            server.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    6, 0.25, 0.25, 0.25, 0.02);
        }
    }

    /** 读取呼吸法层数（amplifier + 1，封顶 {@link #MAX_POISE_STACKS}），没有则该 0。 */
    public static int poiseStacks(LivingEntity entity) {
        MobEffectInstance inst = entity.getEffect(ModEffects.POISE.get());
        if (inst == null) {
            return 0;
        }
        return Math.min(MAX_POISE_STACKS, inst.getAmplifier() + 1);
    }

    /**
     * 给实体叠加呼吸法层数（封顶 30 层）。
     *
     * @param entity 目标
     * @param stacks 叠加层数
     * @return 叠加后的实际层数
     */
    public static int addPoise(LivingEntity entity, int stacks) {
        if (entity == null || stacks <= 0) {
            return poiseStacks(entity);
        }
        int now = poiseStacks(entity);
        if (now >= MAX_POISE_STACKS) {
            return MAX_POISE_STACKS;
        }
        int next = Math.min(MAX_POISE_STACKS, now + stacks);
        entity.addEffect(new MobEffectInstance(ModEffects.POISE.get(), 20 * 600, next - 1, false, true, true));
        return next;
    }

    private PoiseCritEvents() {
    }
}