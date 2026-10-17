package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 充能（Charge）：原著「特定技能发动附加效果所需的资源。最大 20 层。
 * 回合结束时本效果的层数降低 1 层。」
 * <p>本模组把「1 回合」映射为 {@link #TICKS_PER_TURN}（40 tick = 2 秒）。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class ChargeEvents {
    /** 一个「回合」的 tick 数。 */
    public static final int TICKS_PER_TURN = 40;
    /** 原著上限。 */
    public static final int MAX_STACKS = 20;
    /** 累计消耗层数（NBT）。 */
    public static final String TAG_SPENT = "MoonChargeSpent";

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || entity.tickCount % TICKS_PER_TURN != 0) {
            return;
        }
        MobEffectInstance charge = entity.getEffect(ModEffects.CHARGE.get());
        if (charge == null) {
            return;
        }
        int stacks = charge.getAmplifier() + 1;
        entity.removeEffect(ModEffects.CHARGE.get());
        if (stacks > 1) {
            entity.addEffect(new MobEffectInstance(ModEffects.CHARGE.get(), charge.getDuration(), stacks - 2, false, true));
        }
    }

    /** 增加充能层数（上限 20）。 */
    public static void addCharge(LivingEntity entity, int amount) {
        MobEffectInstance current = entity.getEffect(ModEffects.CHARGE.get());
        int stacks = (current == null ? 0 : current.getAmplifier() + 1) + amount;
        stacks = Math.min(MAX_STACKS, stacks);
        if (stacks <= 0) {
            entity.removeEffect(ModEffects.CHARGE.get());
            return;
        }
        entity.addEffect(new MobEffectInstance(ModEffects.CHARGE.get(), 20 * 60, stacks - 1, false, true));
    }

    /** 消耗充能，返回实际消耗层数。 */
    public static int consumeCharge(LivingEntity entity, int amount) {
        MobEffectInstance current = entity.getEffect(ModEffects.CHARGE.get());
        if (current == null) {
            return 0;
        }
        int stacks = current.getAmplifier() + 1;
        int spent = Math.min(stacks, amount);
        entity.removeEffect(ModEffects.CHARGE.get());
        if (stacks - spent > 0) {
            entity.addEffect(new MobEffectInstance(ModEffects.CHARGE.get(), current.getDuration(), stacks - spent - 1, false, true));
        }
        entity.getPersistentData().putInt(TAG_SPENT,
                entity.getPersistentData().getInt(TAG_SPENT) + spent);
        return spent;
    }

    private ChargeEvents() {
    }
}