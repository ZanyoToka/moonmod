package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 专属技能键（客户端）：N = 天杀，M = 将我抹去，也将你抹去。
 * R 留给 Epic Fight 的战斗模式切换。
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModKeybinds {
    public static final String CATEGORY = "key.categories.moonproject";

    public static final KeyMapping HEAVENLY_KILL = new KeyMapping(
            "key.moonproject.heavenly_kill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);
    public static final KeyMapping ERASE = new KeyMapping(
            "key.moonproject.erase", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY);

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(HEAVENLY_KILL);
        event.register(ERASE);
    }

    private ModKeybinds() {
    }
}