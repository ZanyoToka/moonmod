package com.moonproject.moonmod.item;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * E.G.O 装甲的材质。
 *
 * <p><b>设计原则（用户 2026-10-17 追加）：模组里的武器与护甲都必须强于原版最强装备，
 * 后续的新 BOSS 同理。</b></p>
 *
 * <p>对照原版最强的下界合金：护甲值 3/8/6/3（合计 20）、韧性 3.0、击退抗性 0.1、附魔性 15、耐久倍率 37。
 * 本材质：护甲值 4/9/7/4（合计 24）、韧性 4.0、击退抗性 0.25、附魔性 25、耐久倍率 45 —— 全项更高。</p>
 */
public class EgoArmorMaterial implements ArmorMaterial {
    public static final EgoArmorMaterial INSTANCE = new EgoArmorMaterial();

    private static final int DURABILITY_MULTIPLIER = 45;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        int factor = switch (type) {
            case HELMET -> 11;
            case CHESTPLATE -> 16;
            case LEGGINGS -> 15;
            case BOOTS -> 13;
            default -> 10;
        };
        return DURABILITY_MULTIPLIER * factor;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 4;
            case CHESTPLATE -> 9;
            case LEGGINGS -> 7;
            case BOOTS -> 4;
            default -> 3;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 25;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_NETHERITE;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModItems.EGO_CORE.get());
    }

    /** 贴图路径用的名字：assets/moonproject/textures/models/armor/ego_layer_*.png */
    @Override
    public String getName() {
        return MoonProjectMod.MODID + ":ego";
    }

    @Override
    public float getToughness() {
        return 4.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.25F;
    }

    private EgoArmorMaterial() {
    }
}
