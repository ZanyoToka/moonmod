package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 粒子注册表。
 *
 * <p><b>白盒：粒子是怎么出现又怎么消失的？</b></p>
 * <ol>
 *   <li>这里只注册「粒子类型」（一个 id）。它不带任何外观信息——外观在客户端由
 *       {@code client/particle/*} 的 Provider 决定，贴图在
 *       {@code assets/moonproject/textures/particle/}，帧表在
 *       {@code assets/moonproject/particles/<名字>.json}。</li>
 *   <li>服务端用 {@code ServerLevel#sendParticles(...)} 广播，客户端收到后按类型找 Provider
 *       生成一个 {@code TextureSheetParticle} 实例，由客户端自己逐帧更新与渲染。</li>
 *   <li>粒子<b>不参与存档</b>：服务端只负责「发一次出生指令」，之后完全由客户端模拟。
 *       所以粒子数量是纯表现，不影响性能判定，也不会写进区块。</li>
 * </ol>
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MoonProjectMod.MODID);

    /** 记忆余烬：拔刀、记忆灼烧时从身上飘起的橙白火星。 */
    public static final RegistryObject<SimpleParticleType> MEMORY_EMBER =
            PARTICLES.register("memory_ember", () -> new SimpleParticleType(false));
    /** 焦黑斩痕：命中时崩落的深色碎屑。 */
    public static final RegistryObject<SimpleParticleType> BLACK_SLASH =
            PARTICLES.register("black_slash", () -> new SimpleParticleType(false));
    /** 概念残片：抹去技能触发的冷白碎片。 */
    public static final RegistryObject<SimpleParticleType> CONCEPT_SHARD =
            PARTICLES.register("concept_shard", () -> new SimpleParticleType(false));
}
