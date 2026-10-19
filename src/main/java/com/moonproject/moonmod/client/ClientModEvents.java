package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.client.gui.ExpBurnerScreen;
import com.moonproject.moonmod.client.gui.ResonanceCollectorScreen;
import com.moonproject.moonmod.client.renderer.ScorchedGirlRenderer;
import com.moonproject.moonmod.init.ModEntities;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** 客户端初始化：注册 GUI 与实体渲染器。 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.EXP_BURNER_MENU.get(), ExpBurnerScreen::new);
            MenuScreens.register(ModMenus.RESONANCE_COLLECTOR_MENU.get(), ResonanceCollectorScreen::new);
        });
    }

    /** 实体渲染器注册。 */
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SCORCHED_GIRL.get(), ScorchedGirlRenderer::new);
    }
}