package com.moonproject.moonmod.item;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组物品注册表。
 * 每新增一个物品：在这里加一行 + 语言文件加名字 + 模型文件 + 贴图。
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MoonProjectMod.MODID);

    // ===== 材料 =====

    /** 异想体碎片：基础材料，后续由惩罚鸟等异想体掉落。 */
    public static final RegistryObject<Item> ABNORMALITY_FRAGMENT = ITEMS.register("abnormality_fragment",
            () -> new Item(new Item.Properties()));

    /** E.G.O 核心：由异想体碎片提炼的高级材料。 */
    public static final RegistryObject<Item> EGO_CORE = ITEMS.register("ego_core",
            () -> new Item(new Item.Properties()));

    // ===== 武器 =====

    /**
     * E.G.O 武器「悔恨」（一罪与百善）：
     * 面板伤害 <b>10</b>（1 + 下界合金 3 + 6）、攻速 1.8 次/秒、耐久 2031 ——
     * 全面强于原版最强的下界合金剑（面板 8 / 1.6 次每秒），符合"模组装备强于原版最强"原则。
     * 阶段二会给它加粒子特效与对亡灵特攻。
     */
    public static final RegistryObject<Item> REGRET = ITEMS.register("regret",
            () -> new SwordItem(Tiers.NETHERITE, 6, -2.2F, new Item.Properties().fireResistant()));

    // ===== E.G.O 装甲（4 件，全项强于下界合金；详见 EgoArmorMaterial）=====

    /** E.G.O 头盔：护甲 4 / 韧性 4 / 击退抗性 0.25。 */
    public static final RegistryObject<Item> EGO_HELMET = ITEMS.register("ego_helmet",
            () -> new ArmorItem(EgoArmorMaterial.INSTANCE, ArmorItem.Type.HELMET,
                    new Item.Properties().fireResistant()));

    /** E.G.O 胸甲：护甲 9 / 韧性 4。 */
    public static final RegistryObject<Item> EGO_CHESTPLATE = ITEMS.register("ego_chestplate",
            () -> new ArmorItem(EgoArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().fireResistant()));

    /** E.G.O 护腿：护甲 7 / 韧性 4。 */
    public static final RegistryObject<Item> EGO_LEGGINGS = ITEMS.register("ego_leggings",
            () -> new ArmorItem(EgoArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().fireResistant()));

    /** E.G.O 靴子：护甲 4 / 韧性 4。 */
    public static final RegistryObject<Item> EGO_BOOTS = ITEMS.register("ego_boots",
            () -> new ArmorItem(EgoArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS,
                    new Item.Properties().fireResistant()));

    /**
     * 天杀星刀·阿赖耶识（设计规格书 5.2b）：
     * 鞘击 / 拔刀双形态，面板伤害与攻速由 AlayaAttributeHandler 按形态下发，数值全在 Config。
     */
    /** 脑啡肽：从异想体提取的资源，后续用于在工作方块合成 E.G.O 装备。 */
    public static final RegistryObject<Item> ENKEPHALIN = ITEMS.register("enkephalin",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ALAYA_VIJNANA = ITEMS.register("alaya_vijnana",
            () -> new AlayaVijnanaItem(new Item.Properties().stacksTo(1).fireResistant()));
}
