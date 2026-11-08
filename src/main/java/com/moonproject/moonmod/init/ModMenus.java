package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.ai.npc.inventory.NpcInventoryMenu;
import com.moonproject.moonmod.gui.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MoonProjectMod.MODID);

    // 1. 经验焚书炉
    public static final RegistryObject<MenuType<ExpBurnerMenu>> EXP_BURNER_MENU =
            MENUS.register("exp_burner_menu", () -> IForgeMenuType.create(ExpBurnerMenu::new));

    // 1.5 场谐振能源采集单元
    public static final RegistryObject<MenuType<ResonanceCollectorMenu>> RESONANCE_COLLECTOR_MENU =
            MENUS.register("resonance_collector_menu", () -> IForgeMenuType.create(ResonanceCollectorMenu::new));

    // 2. 异想体收容单元
    public static final RegistryObject<MenuType<ContainmentUnitMenu>> CONTAINMENT_UNIT_MENU =
            MENUS.register("containment_unit_menu", () -> IForgeMenuType.create(ContainmentUnitMenu::new));

    // 3. 脑啡肽提取终端
    public static final RegistryObject<MenuType<EnkephalinExtractorMenu>> ENKEPHALIN_EXTRACTOR_MENU =
            MENUS.register("enkephalin_extractor_menu", () -> IForgeMenuType.create(EnkephalinExtractorMenu::new));

    // 4. 脑叶警报广播机
    public static final RegistryObject<MenuType<MeltdownSirenMenu>> MELTDOWN_SIREN_MENU =
            MENUS.register("meltdown_siren_menu", () -> IForgeMenuType.create(MeltdownSirenMenu::new));

    // 5. 总类层装订台
    public static final RegistryObject<MenuType<BookbindingTableMenu>> BOOKBINDING_TABLE_MENU =
            MENUS.register("bookbinding_table_menu", () -> IForgeMenuType.create(BookbindingTableMenu::new));

    // 6. 人格抽取共鸣舱
    public static final RegistryObject<MenuType<IdentityExtractorMenu>> IDENTITY_EXTRACTOR_MENU =
            MENUS.register("identity_extractor_menu", () -> IForgeMenuType.create(IdentityExtractorMenu::new));

    // 7. 梅菲斯特引擎核心
    public static final RegistryObject<MenuType<MephistophelesEngineMenu>> MEPHISTOPHELES_ENGINE_MENU =
            MENUS.register("mephistopheles_engine_menu", () -> IForgeMenuType.create(MephistophelesEngineMenu::new));

    // 8. K公司再生安瓿灌装机
    public static final RegistryObject<MenuType<KCorpInjectorMenu>> KCORP_INJECTOR_MENU =
            MENUS.register("kcorp_injector_menu", () -> IForgeMenuType.create(KCorpInjectorMenu::new));

    // 9. 黄金枝基座
    public static final RegistryObject<MenuType<GoldenBoughShrineMenu>> GOLDEN_BOUGH_SHRINE_MENU =
            MENUS.register("golden_bough_shrine_menu", () -> IForgeMenuType.create(GoldenBoughShrineMenu::new));

    // 10. 都市重装防爆壁
    public static final RegistryObject<MenuType<ReinforcedPlatingMenu>> REINFORCED_PLATING_MENU =
            MENUS.register("reinforced_plating_menu", () -> IForgeMenuType.create(ReinforcedPlatingMenu::new));

    // 11. AI NPC 随身行囊与管理界面 (Maid-style Inventory Menu)
    public static final RegistryObject<MenuType<NpcInventoryMenu>> NPC_INVENTORY_MENU =
            MENUS.register("npc_inventory_menu", () -> IForgeMenuType.create(NpcInventoryMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
