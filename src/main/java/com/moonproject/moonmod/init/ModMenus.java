package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.gui.ExpBurnerMenu;
import com.moonproject.moonmod.gui.ResonanceCollectorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MoonProjectMod.MODID);

    public static final RegistryObject<MenuType<ExpBurnerMenu>> EXP_BURNER_MENU =
            MENUS.register("exp_burner_menu", () -> IForgeMenuType.create(ExpBurnerMenu::new));

    public static final RegistryObject<MenuType<ResonanceCollectorMenu>> RESONANCE_COLLECTOR_MENU =
            MENUS.register("resonance_collector_menu", () -> IForgeMenuType.create(ResonanceCollectorMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}