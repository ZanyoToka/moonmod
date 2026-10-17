package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.network.ModNetwork;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 专属技能键（客户端）：R = 天杀，V = 将我抹去，也将你抹去。
 *
 * <p>按键后通过 {@link ModNetwork} 把技能编号发给服务端执行。</p>
 */
public final class ModKeybinds {
    public static final String CATEGORY = "key.categories.moonproject";

    public static final KeyMapping HEAVENLY_KILL = new KeyMapping(
            "key.moonproject.heavenly_kill", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);

    public static final KeyMapping ERASE = new KeyMapping(
            "key.moonproject.erase", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    /** mod 总线：注册按键（只在客户端）。 */
    @Mod.EventBusSubscriber(modid = MoonProjectMod.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(HEAVENLY_KILL);
            event.register(ERASE);
        }
    }

    /** 游戏总线：每 tick 检查按键并发送网络包（只在客户端）。 */
    @Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, value = Dist.CLIENT)
    public static final class Handler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
                return;
            }
            while (HEAVENLY_KILL.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSkillMessage(0));
            }
            while (ERASE.consumeClick()) {
                ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSkillMessage(1));
            }
        }
    }

    private ModKeybinds() {
    }
}
