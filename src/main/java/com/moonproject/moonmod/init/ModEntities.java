package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.entity.ScorchedGirlEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 实体注册表。
 *
 * <p>新增实体时按下面 {@link #SCORCHED_GIRL} 的写法注册，并配套：</p>
 * <ol>
 *   <li>entity/ 包下的实体类；</li>
 *   <li>client/renderer 包下的渲染器 + 在 ClientModEvents 里注册；</li>
 *   <li>lang 里加名字；</li>
 *   <li>需要的话加刷怪蛋 / 掉落表。</li>
 * </ol>
 */
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MoonProjectMod.MODID);

    /** 焦化少女（TETH）：靠近玩家自爆。 */
    public static final RegistryObject<EntityType<ScorchedGirlEntity>> SCORCHED_GIRL =
            ENTITIES.register("scorched_girl", () -> EntityType.Builder
                    .of(ScorchedGirlEntity::new, MobCategory.MONSTER)
                    .sized(0.55F, 1.0F)
                    .clientTrackingRange(8)
                    .build("scorched_girl"));

    private ModEntities() {
    }
}
