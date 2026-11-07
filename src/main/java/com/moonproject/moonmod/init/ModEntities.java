package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.entity.ScorchedGirlEntity;
import com.moonproject.moonmod.entity.SeatEntity;
import com.moonproject.moonmod.entity.SlashScarEntity;
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

    /** 焦化少女（TETH）：靠近玩家自爆。 */
    public static final RegistryObject<EntityType<ScorchedGirlEntity>> SCORCHED_GIRL =
            ENTITY_TYPES.register("scorched_girl", () -> EntityType.Builder
                    .of(ScorchedGirlEntity::new, MobCategory.MONSTER)
                    .sized(0.55F, 1.0F)
                    .clientTrackingRange(8)
                    .build("scorched_girl"));

    /** 刀光残留：斩击留在空气里的弧光，纯装饰、无碰撞、不存档。 */
    public static final RegistryObject<EntityType<SlashScarEntity>> SLASH_SCAR =
            ENTITY_TYPES.register("slash_scar", () -> EntityType.Builder
                    .of(SlashScarEntity::new, MobCategory.MISC)
                    .sized(0.05F, 0.05F)
                    .noSave()
                    .noSummon()
                    .clientTrackingRange(10)
                    .updateInterval(40)
                    .build("slash_scar"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
