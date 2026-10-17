package com.moonproject.moonmod.client;

/** 客户端理智值缓存（由 S2C 网络包写入，供 HUD 读取）。 */
public final class SanityClient {
    private static int sanity = 0;

    public static int get() {
        return sanity;
    }

    public static void set(int value) {
        sanity = value;
    }

    private SanityClient() {
    }
}