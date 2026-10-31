package com.moonproject.moonmod.trinket;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 把饰品能力挂到玩家身上，并处理登录时的数据同步。 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class TrinketEvents {

    private static final ResourceLocation KEY =
            ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "trinkets");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }
        if (event.getObject().getCapability(TrinketProvider.TRINKET).isPresent()) {
            return;
        }
        event.addCapability(KEY, new TrinketProvider());
    }

    private TrinketEvents() {
    }
}