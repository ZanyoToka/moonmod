package com.moonproject.moonmod.sanity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * 理智值（Sanity）：原著里范围 -45 ~ +45，囚徒初始 0。
 * 存在玩家持久化数据里（Forge 会自动随存档保存），键名 {@link #TAG}。
 */
public final class Sanity {
    public static final String TAG = "MoonSanity";
    /** 理智伤害转体力伤害的倍率（阿鼻叫唤）。 */
    public static final String TAG_ABYSS_CALL = "MoonAbyssCall";
    /** 固定恐慌标记。 */
    public static final String TAG_FIXED_PANIC = "MoonFixedPanic";

    public static final int MIN = -45;
    public static final int MAX = 45;

    public static int get(Player player) {
        return player.getPersistentData().getInt(TAG);
    }

    public static void set(Player player, int value) {
        player.getPersistentData().putInt(TAG, clamp(value));
    }

    /** 返回实际变化量（可能被上下限截断）。 */
    public static int add(Player player, int delta) {
        int before = get(player);
        int after = clamp(before + delta);
        if (after != before) {
            player.getPersistentData().putInt(TAG, after);
        }
        return after - before;
    }

    public static int clamp(int value) {
        return Math.max(MIN, Math.min(MAX, value));
    }

    /** 阿鼻叫唤：理智伤害改为 3 倍体力伤害。 */
    public static boolean hasAbyssCall(Player player) {
        return player.getPersistentData().getBoolean(TAG_ABYSS_CALL);
    }

    public static void setAbyssCall(Player player, boolean on) {
        player.getPersistentData().putBoolean(TAG_ABYSS_CALL, on);
    }

    /** 固定恐慌：恐慌类型不会被改变。 */
    public static boolean hasFixedPanic(Player player) {
        return player.getPersistentData().getBoolean(TAG_FIXED_PANIC);
    }

    public static void setFixedPanic(Player player, boolean on) {
        player.getPersistentData().putBoolean(TAG_FIXED_PANIC, on);
    }

    /** 施加理智伤害：有阿鼻叫唤时改为 3 倍体力伤害。 */
    public static void damage(Player player, int amount) {
        if (amount <= 0) {
            return;
        }
        if (hasAbyssCall(player)) {
            player.hurt(player.damageSources().magic(), amount * 3.0F);
        } else {
            add(player, -amount);
        }
    }

    private Sanity() {
    }
}