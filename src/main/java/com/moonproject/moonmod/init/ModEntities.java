package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 实体注册表（规划第 4 章：异想体 / Boss / NPC / 弹幕）。
 * 当前为空骨架，新增实体时按下述示例注册：
 *
 * <pre>
 * public static final RegistryObject&lt;EntityType&lt;NothingThereEntity&gt;&gt; NOTHING_THERE =
 *     ENTITIES.register("nothing_there",
 *         () -&gt; EntityType.Builder.of(NothingThereEntity::new, MobCategory.MONSTER)
 *             .sized(1.2F, 2.7F)
 *             .build("nothing_there"));
 * </pre>
 *
 * 配套工作（每个实体 4 件套）：
 * 1. entity/ 包下的实体类（继承 {@link com.moonproject.moonmod.entity.AbnormalityEntity}）；
 * 2. GeckoLib 骨骼模型与动画（assets/moonproject/animmodels/）；
 * 3. data/moonproject/ 生成数据（鸡蛋 / 自然生成 / 掉落表）；
 * 4. lang/zh_cn.json 与 en_us.json 增加名字。
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MoonProjectMod.MODID);
}
