package com.moonproject.moonmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 「异想体饰品」这一类物品的基类，与 {@link AbnormalityCoreItem} 一一对应。
 *
 * <p>每个核心 {@code coreId} 有且只有一个饰品；映射关系在
 * {@code com.moonproject.moonmod.item.CoreTrinketRegistry} 里登记。</p>
 */
public class AbnormalityTrinketItem extends Item {

    private final String coreId;
    private final String slot;

    public AbnormalityTrinketItem(String coreId, String slot, Properties properties) {
        super(properties);
        this.coreId = coreId;
        this.slot = slot;
    }

    /** 对应的异想体标识（与核心的 coreId 相同）。 */
    public String getCoreId() {
        return coreId;
    }

    /** 佩戴槽位：head / neck / ring / hand ……（接入 Curios 后使用）。 */
    public String getSlot() {
        return slot;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack);
    }

    /** 在物品提示里标注「这是一件异想体饰品」并显示佩戴槽位。 */
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level,
                                java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6异想体饰品").withStyle(net.minecraft.ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7佩戴部位：§e" + slotName(slot))
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7对应核心：§d" + coreName(coreId))
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    private static String slotName(String slot) {
        return switch (slot) {
            case "head" -> "头饰";
            case "neck" -> "项链";
            case "ring" -> "戒指";
            case "hand" -> "手部";
            default -> slot;
        };
    }

    private static String coreName(String id) {
        return switch (id) {
            case "scorched_girl" -> "焦化少女";
            default -> id;
        };
    }
}