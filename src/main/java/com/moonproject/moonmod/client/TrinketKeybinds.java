package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 饰品快捷键（默认 J，Curios 自身默认 G 且背包界面提供饰品按钮）。
 */
public final class TrinketKeybinds {

    public static final KeyMapping OPEN_TRINKETS = new KeyMapping(
            "key.moonproject.trinkets", GLFW.GLFW_KEY_J, "key.categories.moonproject");

    /** 注册按键（MOD 总线）。 */
    @Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registrar {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_TRINKETS);
        }

        private Registrar() {
        }
    }

    /** 每客户端 tick 检查一次按键（FORGE 总线）。 */
    @Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, value = Dist.CLIENT)
    public static final class Handler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) {
                return;
            }
            while (OPEN_TRINKETS.consumeClick()) {
                if (mc.getConnection() != null) {
                    mc.getConnection().sendCommand("trinket");
                }
            }
        }

        private Handler() {
        }
    }

    private TrinketKeybinds() {
    }
}
