package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 理智值的基础变化规则：
 * <ul>
 *   <li>攻<strong>击</strong>敌方单位一次 → 理智 <strong>+1</strong></li>
 *   <li>受<strong>击</strong>一次 → 理智 <strong>-5</strong></li>
 *   <li>死亡 → 理智<strong>重置为 0</strong></li>
 * </ul>
 * 手持阿赖耶识时由 {@code AbyssCallEvents} 把理智锁在 -44，会覆盖这里的增减。
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class SanityEvents {
    /** 攻击命中敌方单位回复的理智。 */
    public static final int GAIN_ON_HIT = 1;
    /** 受到伤害时扣除的理智。 */
    public static final int LOSS_ON_HURT = 5;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 受击：-5（只对拥有理智值的生物，即玩家与人类型怪物）
        if (event.getEntity() instanceof Player victim) {
            Sanity.add(victim, -LOSS_ON_HURT);
        }

        // 攻击敌方单位：+1
        if (event.getSource().getEntity() instanceof Player attacker
                && event.getEntity() instanceof Enemy
                && !attacker.level().isClientSide()) {
            Sanity.add(attacker, GAIN_ON_HIT);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            Sanity.set(player, 0);
            player.getPersistentData().putString("MoonPanicType", "NONE");
        }
    }

    /** 每 10 tick 把理智值同步给客户端（用于 HUD）。 */
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END
                || event.player.level().isClientSide()
                || event.player.tickCount % 10 != 0) {
            return;
        }
        if (event.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            com.moonproject.moonmod.network.ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new com.moonproject.moonmod.network.ModNetwork.SanityMessage(Sanity.get(serverPlayer)));
        }
    }

    private SanityEvents() {
    }
}