package com.moonproject.moonmod.entity;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.init.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 把所有异想体的属性注册到游戏里（必须有这一步，否则召唤出来没有血量）。 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntityAttributes {

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SCORCHED_GIRL.get(), ScorchedGirlEntity.createAttributes().build());
    }

    private ModEntityAttributes() {
    }
}