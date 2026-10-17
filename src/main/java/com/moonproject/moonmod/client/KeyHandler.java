package com.moonproject.moonmod.client;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端按键处理：每 tick 检查 G / H，按下就向服务端发包释放对应技能。
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, value = Dist.CLIENT)
public final class KeyHandler {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
            return;
        }
        while (ModKeybinds.HEAVENLY_KILL.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSkillMessage(0));
        }
        while (ModKeybinds.ERASE.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.CastSkillMessage(1));
        }
    }

    private KeyHandler() {
    }
}