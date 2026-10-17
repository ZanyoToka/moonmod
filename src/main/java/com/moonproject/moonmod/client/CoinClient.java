package com.moonproject.moonmod.client;

/** 客户端硬币结果缓存（由 S2C 包写入，HUD 读取）。 */
public final class CoinClient {
    private static int facesMask = 0;
    private static int coinCount = 0;
    private static double multiplier = 1.0D;
    private static long expireAt = 0L;
    private static final long SHOW_MS = 4000L;

    public static void set(int mask, int count, double mult) {
        facesMask = mask;
        coinCount = count;
        multiplier = mult;
        expireAt = System.currentTimeMillis() + SHOW_MS;
    }

    public static boolean visible() {
        return coinCount > 0 && System.currentTimeMillis() < expireAt;
    }

    public static boolean isHeads(int index) {
        return (facesMask & (1 << index)) != 0;
    }

    public static int count() {
        return coinCount;
    }

    public static double getMultiplier() {
        return multiplier;
    }

    private CoinClient() {
    }
}