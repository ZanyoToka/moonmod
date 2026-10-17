package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.client.gui.ExpBurnerScreen;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.EXP_BURNER_MENU.get(), ExpBurnerScreen::new);
        });
    }
}