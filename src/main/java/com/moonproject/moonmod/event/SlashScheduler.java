package com.moonproject.moonmod.event;

import com.moonproject.moonmod.config.MoonProjectConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 「快慢刀 = 斩击延迟生效」的调度器。
 *
 * <p>原著（《边狱巴士》/《废墟图书馆》）的攻击不是瞬间结算：挥刀之后要过一个前摇才落地，
 * 快刀前摇短、慢刀前摇长，而且慢刀有被抢断的风险。这里用一张待结算表还原这个手感：</p>
 * <ul>
 *   <li>快刀：{@code fastSlashDelayTicks}（默认 4 tick = 0.2 秒）后落地；</li>
 *   <li>慢刀：{@code slowSlashDelayTicks}（默认 12 tick = 0.6 秒）后落地，
 *       期间挥刀者被击中则<b>被打断</b>（{@link #cancelSlow(Player)}）。</li>
 * </ul>
 */
public final class SlashScheduler {
    private static final List<PendingSlash> PENDING = new ArrayList<>();

    private static final class PendingSlash {
        final Player player;
        final LivingEntity target;
        final float damage;
        final boolean slow;
        int ticksLeft;

        PendingSlash(Player player, LivingEntity target, float damage, boolean slow, int ticksLeft) {
            this.player = player;
            this.target = target;
            this.damage = damage;
            this.slow = slow;
            this.ticksLeft = ticksLeft;
        }
    }

    /** 登记一次延迟斩击。 */
    public static void schedule(Player player, LivingEntity target, float damage, boolean slow) {
        schedule(player, target, damage, slow, 0);
    }

    /** 指定额外延迟的变体：多硬币技能用它把各段错开落地。 */
    public static void schedule(Player player, LivingEntity target, float damage, boolean slow, int extraTicks) {
        int delay = (slow ? MoonProjectConfig.slowSlashDelayTicks() : MoonProjectConfig.fastSlashDelayTicks())
                + extraTicks;
        if (delay <= 0) {
            CombatEvents.applyDelayedHit(player, target, damage, slow);
            return;
        }
        PENDING.add(new PendingSlash(player, target, damage, slow, delay));
    }

    /** 被攻击时打断尚未落地的慢刀。 */
    public static void cancelSlow(Player player) {
        PENDING.removeIf(pending -> pending.player == player && pending.slow);
    }

    /** 由 {@code CombatEvents} 的玩家 tick 处理器驱动；每个玩家只推进自己的斩击。 */
    public static void tick(Player player) {
        if (PENDING.isEmpty()) {
            return;
        }
        Iterator<PendingSlash> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            PendingSlash pending = iterator.next();
            if (pending.player != player) {
                continue;
            }
            if (--pending.ticksLeft > 0) {
                continue;
            }
            iterator.remove();
            if (pending.player.isRemoved() || !pending.target.isAlive()) {
                continue;
            }
            if (pending.player.level() != pending.target.level()) {
                continue;
            }
            CombatEvents.applyDelayedHit(pending.player, pending.target, pending.damage, pending.slow);
        }
    }

    private SlashScheduler() {
    }
}
