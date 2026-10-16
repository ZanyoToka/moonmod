package com.moonproject.moonmod.item;

import com.moonproject.moonmod.MoonProjectMod;
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
     * 钻石级属性，攻击伤害 7（3+4），攻速 -2.4。
     * 阶段二会给它加粒子特效与对亡灵特攻。
     */
    public static final RegistryObject<Item> REGRET = ITEMS.register("regret",
            () -> new SwordItem(Tiers.DIAMOND, 4, -2.4F, new Item.Properties()));
}
