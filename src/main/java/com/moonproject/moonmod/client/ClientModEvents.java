package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.client.gui.*;
import com.moonproject.moonmod.init.ModEntities;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.EXP_BURNER_MENU.get(), ExpBurnerScreen::new);
            MenuScreens.register(ModMenus.CONTAINMENT_UNIT_MENU.get(), ContainmentUnitScreen::new);
            MenuScreens.register(ModMenus.ENKEPHALIN_EXTRACTOR_MENU.get(), EnkephalinExtractorScreen::new);
            MenuScreens.register(ModMenus.MELTDOWN_SIREN_MENU.get(), MeltdownSirenScreen::new);
            MenuScreens.register(ModMenus.BOOKBINDING_TABLE_MENU.get(), BookbindingTableScreen::new);
            MenuScreens.register(ModMenus.IDENTITY_EXTRACTOR_MENU.get(), IdentityExtractorScreen::new);
            MenuScreens.register(ModMenus.MEPHISTOPHELES_ENGINE_MENU.get(), MephistophelesEngineScreen::new);
            MenuScreens.register(ModMenus.KCORP_INJECTOR_MENU.get(), KCorpInjectorScreen::new);
            MenuScreens.register(ModMenus.GOLDEN_BOUGH_SHRINE_MENU.get(), GoldenBoughShrineScreen::new);
            MenuScreens.register(ModMenus.REINFORCED_PLATING_MENU.get(), ReinforcedPlatingScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEAT.get(), SeatRenderer::new);
    }
}
