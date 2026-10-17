package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.sanity.PanicType;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 恐慌类型系统：理智值低于 {@link #PANIC_THRESHOLD} 时，玩家陷入一种恐慌类型。
 * <p>固定恐慌（{@link Sanity#hasFixedPanic}）生效时，恐慌类型不再改变。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class PanicEvents {
    public static final String TAG_PANIC = "MoonPanicType";
    /** 原著里理智低于该值会进入恐慌。 */
    public static final int PANIC_THRESHOLD = -25;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        if (player.tickCount % 20 != 0) {
            return;
        }
        int sanity = Sanity.get(player);
        CompoundTag data = player.getPersistentData();
        PanicType current = PanicType.valueOf(data.getString(TAG_PANIC).isEmpty() ? "NONE" : data.getString(TAG_PANIC));

        if (sanity > PANIC_THRESHOLD) {
            // 理智恢复 -> 解除恐慌
            if (current != PanicType.NONE) {
                data.putString(TAG_PANIC, PanicType.NONE.name());
            }
            return;
        }

        // 需要恐慌：固定恐慌时保持现有类型
        if (current == PanicType.NONE || !Sanity.hasFixedPanic(player)) {
            if (current == PanicType.NONE) {
                current = PanicType.values()[1 + player.getRandom().nextInt(PanicType.values().length - 1)];
                data.putString(TAG_PANIC, current.name());
            }
        }

        // 施加对应效果（每 20 tick 续一次）
        int duration = 60;
        switch (current) {
            case WRATH -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
            }
            case DESPAIR -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
            }
            case PANIC -> {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1));
            }
            default -> {
            }
        }
    }

    private PanicEvents() {
    }
}