package com.moonproject.moonmod.item;

import java.util.HashMap;
import java.util.Map;

/**
 * 异想体核心 ↔ 异想体饰品的映射表（一一对应）。
 *
 * <p>每个 {@code coreId} 对应唯一一个饰品物品。新增异想体时在这里登记一行即可。</p>
 */
public final class CoreTrinketRegistry {

    /** coreId → 饰品物品。 */
    private static final Map<String, AbnormalityTrinketItem> CORE_TO_TRINKET = new HashMap<>();

    /** 登记一对映射。 */
    public static void register(AbnormalityCoreItem core, AbnormalityTrinketItem trinket) {
        CORE_TO_TRINKET.put(core.getCoreId(), trinket);
    }

    /** 按 coreId 取对应的饰品；没有则返回 null。 */
    public static AbnormalityTrinketItem trinketOf(String coreId) {
        return CORE_TO_TRINKET.get(coreId);
    }

    private CoreTrinketRegistry() {
    }
}