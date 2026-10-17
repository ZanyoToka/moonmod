package com.moonproject.moonmod.sanity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Piglin;
import net.minecraft.world.entity.monster.PiglinBrute;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;

/**
 * 理智值（Sanity）：范围 -45 ~ +45，玩家与「人类型怪物」拥有。
 * <p>非人型怪物（蜘蛛、苦力怕、骷髅、异想体等）**没有理智值**，
 * 沉沦对它们改为造成忧郁属性伤害（原著规则）。</p>
 */
public final class Sanity {
    public static final String TAG = "MoonSanity";
    public static final String TAG_ABYSS_CALL = "MoonAbyssCall";
    public static final String TAG_FIXED_PANIC = "MoonFixedPanic";

    public static final int MIN = -45;
    public static final int MAX = 45;

    /** 该生物是否拥有理智值：玩家 + 人类型怪物（灾厄村民、女巫、猪灵）。 */
    public static boolean hasSanity(LivingEntity entity) {
        if (entity instanceof Player) {
            return true;
        }
        return entity instanceof AbstractIllager
                || entity instanceof Witch
                || entity instanceof Piglin
                || entity instanceof PiglinBrute;
    }

    public static int get(LivingEntity entity) {
        return entity.getPersistentData().getInt(TAG);
    }

    public static int get(Player player) {
        return get((LivingEntity) player);
    }

    public static void set(LivingEntity entity, int value) {
        entity.getPersistentData().putInt(TAG, clamp(value));
    }

    public static void set(Player player, int value) {
        set((LivingEntity) player, value);
    }

    /** 返回实际变化量（可能被上下限截断）。 */
    public static int add(LivingEntity entity, int delta) {
        int before = get(entity);
        int after = clamp(before + delta);
        if (after != before) {
            entity.getPersistentData().putInt(TAG, after);
        }
        return after - before;
    }

    public static int add(Player player, int delta) {
        return add((LivingEntity) player, delta);
    }

    public static int clamp(int value) {
        return Math.max(MIN, Math.min(MAX, value));
    }

    public static boolean hasAbyssCall(Player player) {
        return player.getPersistentData().getBoolean(TAG_ABYSS_CALL);
    }

    public static void setAbyssCall(Player player, boolean on) {
        player.getPersistentData().putBoolean(TAG_ABYSS_CALL, on);
    }

    public static boolean hasFixedPanic(Player player) {
        return player.getPersistentData().getBoolean(TAG_FIXED_PANIC);
    }

    public static void setFixedPanic(Player player, boolean on) {
        player.getPersistentData().putBoolean(TAG_FIXED_PANIC, on);
    }

    /** 对生物施加理智伤害：玩家有阿鼻叫唤时改为 3 倍体力伤害。 */
    public static void damage(LivingEntity entity, int amount) {
        if (amount <= 0) {
            return;
        }
        if (entity instanceof Player player && hasAbyssCall(player)) {
            player.hurt(player.damageSources().magic(), amount * 3.0F);
        } else {
            add(entity, -amount);
        }
    }

    public static void damage(Player player, int amount) {
        damage((LivingEntity) player, amount);
    }

    private Sanity() {
    }
}