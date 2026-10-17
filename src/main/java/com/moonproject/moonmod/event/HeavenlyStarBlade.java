package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * 天杀星刀-阿赖耶识（原著）：
 * <ul>
 *   <li>攻击等级 <strong>+6</strong>（映射为攻击伤害 +6）—— <strong>有时间限制</strong>，
 *       每次命中敌人刷新，持续 {@link #DURATION_TICKS} tick（30 秒）</li>
 *   <li>技能增加的<strong>流血强度额外 +3 级</strong>（见 CombatEvents）</li>
 *   <li>本场战斗中<strong>每击杀 1 名敌方单位，获得 3 层攻击等级提升（最多 6 层）</strong></li>
 * </ul>
 * 注：原著的「速度值 +6」在 MC 里没有对应属性，已按要求舍弃。
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class HeavenlyStarBlade {
    public static final String TAG_KILL_STACKS = "MoonKillStacks";
    public static final String TAG_UNTIL = "MoonHeavenlyStarUntil";

    public static final int MAX_KILL_STACKS = 6;
    public static final int STACKS_PER_KILL = 3;
    /** 攻击等级加成的持续时间（30 秒）。 */
    public static final int DURATION_TICKS = 30 * 20;

    public static final double ATTACK_BONUS = 6.0D;
    public static final double PER_STACK_ATTACK = 1.0D;

    private static final UUID ATK_UUID = UUID.fromString("3e0f7a52-8c1d-4b77-9a21-5d6b0f8c1e10");

    /** 命中敌人时刷新加成时限。 */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getSource().getEntity() instanceof Player attacker && Sanity.hasAbyssCall(attacker)) {
            refresh(attacker);
        }
    }

    /** 击杀累加攻击等级提升层数。 */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player killer) || !Sanity.hasAbyssCall(killer)) {
            return;
        }
        CompoundTag data = killer.getPersistentData();
        int stacks = Math.min(MAX_KILL_STACKS, data.getInt(TAG_KILL_STACKS) + STACKS_PER_KILL);
        data.putInt(TAG_KILL_STACKS, stacks);
        refresh(killer);
    }

    public static void refresh(Player player) {
        player.getPersistentData().putLong(TAG_UNTIL, player.level().getGameTime() + DURATION_TICKS);
    }

    public static boolean isActive(Player player) {
        long until = player.getPersistentData().getLong(TAG_UNTIL);
        return until > 0L && player.level().getGameTime() < until;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) {
            return;
        }
        attack.removeModifier(ATK_UUID);

        if (!Sanity.hasAbyssCall(player) || !isActive(player)) {
            return;
        }
        int killStacks = player.getPersistentData().getInt(TAG_KILL_STACKS);
        attack.addTransientModifier(new AttributeModifier(ATK_UUID, "heavenly_star_blade",
                ATTACK_BONUS + killStacks * PER_STACK_ATTACK, AttributeModifier.Operation.ADDITION));
    }

    private HeavenlyStarBlade() {
    }
}