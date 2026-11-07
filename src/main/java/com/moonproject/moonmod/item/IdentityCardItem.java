package com.moonproject.moonmod.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.moonproject.moonmod.config.MoonProjectConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 「人格卡」：人格抽取共鸣舱的产物，可作为饰品佩戴（Curios 手部槽）。
 *
 * <p>Why 一个物品装所有人格：人格身份存在 NBT（{@link #TAG_CARD_ID}），
 * 以后加新人格只需在 {@link IdentityCardData} 登记一行，不需要新物品、新模型、新贴图，
 * 也不会让 {@code docs/verify_assets.py} 的物品清单膨胀。</p>
 *
 * <p>数值口径：卡面写的是基础值，实际下发 = 基础值 × {@code MoonProjectConfig.identityCardPowerScale()}。</p>
 */
public class IdentityCardItem extends Item implements ICurioItem {

    /** NBT：人格 id（见 {@link IdentityCardData}）。 */
    public static final String TAG_CARD_ID = "CardId";
    /** NBT：人格等级（0 / 1 / 2）。 */
    public static final String TAG_CARD_GRADE = "CardGrade";

    /** 佩戴槽位：手部。 */
    public static final String SLOT = "hands";

    public IdentityCardItem(Properties properties) {
        super(properties);
    }

    // ==================== 制造与读取 ====================

    /** 按人格数据生成一张卡（含悬浮名称）。 */
    public static ItemStack create(IdentityCardData data) {
        ItemStack stack = new ItemStack(ModItems.IDENTITY_CARD.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_CARD_ID, data.id());
        tag.putInt(TAG_CARD_GRADE, data.grade());
        stack.setHoverName(Component.literal(
                        "§" + data.gradeColor().getChar() + "【" + data.gradeLabel() + "人格】" + data.display())
                .withStyle(style -> style.withItalic(false)));
        return stack;
    }

    /** 按 id 生成一张卡；id 未登记返回空栈。 */
    public static ItemStack create(String cardId) {
        IdentityCardData data = IdentityCardData.byId(cardId);
        return data == null ? ItemStack.EMPTY : create(data);
    }

    public static String getCardId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(TAG_CARD_ID);
    }

    public static int getCardGrade(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? IdentityCardData.GRADE_ZERO : tag.getInt(TAG_CARD_GRADE);
    }

    // ==================== Curios 接口 ====================

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        IdentityCardData data = IdentityCardData.of(stack);
        double scale = MoonProjectConfig.identityCardPowerScale();

        put(modifiers, uuid, data, Attributes.MAX_HEALTH, data.maxHealth(), scale,
                AttributeModifier.Operation.ADDITION, "max_health");
        put(modifiers, uuid, data, Attributes.ATTACK_DAMAGE, data.attackDamage(), scale,
                AttributeModifier.Operation.ADDITION, "attack_damage");
        put(modifiers, uuid, data, Attributes.MOVEMENT_SPEED, data.movementSpeed(), scale,
                AttributeModifier.Operation.ADDITION, "movement_speed");
        put(modifiers, uuid, data, Attributes.ARMOR, data.armor(), scale,
                AttributeModifier.Operation.ADDITION, "armor");
        put(modifiers, uuid, data, Attributes.ATTACK_SPEED, data.attackSpeed(), scale,
                AttributeModifier.Operation.MULTIPLY_TOTAL, "attack_speed");

        return modifiers;
    }

    private static void put(Multimap<Attribute, AttributeModifier> modifiers, UUID uuid, IdentityCardData data,
                            Attribute attribute, double base, double scale,
                            AttributeModifier.Operation operation, String suffix) {
        if (base == 0.0D) {
            return;
        }
        double amount = base * scale;
        if (amount == 0.0D) {
            return;
        }
        modifiers.put(attribute, new AttributeModifier(uuid,
                "moonproject:identity_card." + data.id() + "." + suffix, amount, operation));
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // 手持右键直接佩戴
        return true;
    }

    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.2F);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        String identifier = slotContext.identifier();
        return "curio".equals(identifier) || SLOT.equals(identifier);
    }

    // ==================== 提示文本 ====================

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        IdentityCardData data = IdentityCardData.of(stack);
        double scale = MoonProjectConfig.identityCardPowerScale();

        tooltip.add(Component.literal("§6◆ 人格卡 · 佩戴于「手部」◆").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7等级：§" + data.gradeColor().getChar() + data.gradeLabel())
                .withStyle(ChatFormatting.GRAY));

        if (data.hasAttributes()) {
            tooltip.add(Component.literal("§a佩戴加成：").withStyle(ChatFormatting.GREEN));
            entry(tooltip, "最大生命", data.maxHealth() * scale, " 点", 1.0D);
            entry(tooltip, "攻击力", data.attackDamage() * scale, " 点", 1.0D);
            entry(tooltip, "移动速度", data.movementSpeed() * scale, "", 100.0D);
            entry(tooltip, "护甲", data.armor() * scale, " 点", 1.0D);
            entry(tooltip, "攻击速度", data.attackSpeed() * scale, "%", 100.0D);
        } else {
            tooltip.add(Component.literal("§7这张卡没有任何属性加成，只是一段人格。").withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.literal("§8「" + data.flavor() + "」").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("§8[手持右键佩戴，或按饰品栏键放入手部槽]").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void entry(List<Component> tooltip, String label, double value, String suffix, double percentBase) {
        if (value == 0.0D) {
            return;
        }
        String text;
        if (percentBase == 100.0D) {
            text = String.format("  §7- %s §f+%.0f%s", label, value * percentBase, suffix);
        } else if (Math.abs(value - Math.rint(value)) < 1.0E-6D) {
            text = String.format("  §7- %s §f+%.0f%s", label, value, suffix);
        } else {
            text = String.format("  §7- %s §f+%.1f%s", label, value, suffix);
        }
        tooltip.add(Component.literal(text));
    }
}
