package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.entity.SeatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组实体类型注册器。
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MoonProjectMod.MODID);

    public static final RegistryObject<EntityType<SeatEntity>> SEAT =
            ENTITY_TYPES.register("seat", () ->
                    EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                            .sized(0.001F, 0.001F)
                            .noSummon()
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("seat")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
