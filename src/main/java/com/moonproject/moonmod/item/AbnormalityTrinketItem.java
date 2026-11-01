package com.moonproject.moonmod.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

/**
 * 「异想体饰品」基类，接入社区标准饰品接口 {@link ICurioItem}（Curios API）。
 *
 * <p>每个核心 {@code coreId} 有且只有一个对应饰品，映射关系在 {@link CoreTrinketRegistry} 登记。</p>
 * <p>通过 Curios API 原生机制驱动属性修饰符、佩戴槽位匹配、手持右键佩戴与音效，无需轮询 tick 增删。</p>
 */
public class AbnormalityTrinketItem extends Item implements ICurioItem {

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

    /** 佩戴槽位标识（如 head / necklace / ring / hands / curio）。 */
    public String getSlot() {
        return slot;
    }

    // ==================== Curios API 接口实现 ====================

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();

        // 终末火柴之光：焦化少女饰品，提供 +4.0 最大生命值加成（2颗心）
        if ("scorched_girl".equals(coreId)) {
            modifiers.put(Attributes.MAX_HEALTH,
                    new AttributeModifier(uuid, "moonproject:final_match_light_hp", 4.0D, AttributeModifier.Operation.ADDITION));
        }

        return modifiers;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // 支持手持饰品右键快速佩戴
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        // 佩戴时的金属音效
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD, 1.0F, 1.0F);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        String identifier = slotContext.identifier();
        // 通用 curio 槽位均可放入
        if ("curio".equals(identifier)) {
            return true;
        }
        // 兼容别名：neck -> necklace, hand -> hands
        if ("necklace".equals(identifier) && "neck".equals(this.slot)) {
            return true;
        }
        if ("hands".equals(identifier) && "hand".equals(this.slot)) {
            return true;
        }
        return identifier.equals(this.slot);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // 饰品佩戴生效过程中的 tick 回调钩子（供后续扩展粒子/状态结算）
    }

    // ==================== 物品信息与提示 ====================

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6◆ 异想体饰品 ◆").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7佩戴部位：§e" + slotName(slot)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("§7对应核心：§d" + coreName(coreId)).withStyle(ChatFormatting.GRAY));

        // 效果描述
        if ("scorched_girl".equals(coreId)) {
            tooltip.add(Component.literal("§a佩戴效果：最大生命值 +4 (2颗心)").withStyle(ChatFormatting.GREEN));
        }

        tooltip.add(Component.literal("§8[Curios 支持：手持右键佩戴，或按饰品栏键(默认G)放入]").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String slotName(String slot) {
        return switch (slot) {
            case "head" -> "头饰";
            case "neck", "necklace" -> "项链";
            case "ring" -> "戒指";
            case "hand", "hands" -> "手部";
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
