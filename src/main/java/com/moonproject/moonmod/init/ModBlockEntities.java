package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.blockentity.ContainmentUnitBlockEntity;
import com.moonproject.moonmod.blockentity.EnkephalinExtractorBlockEntity;
import com.moonproject.moonmod.blockentity.ExpBurnerBlockEntity;
import com.moonproject.moonmod.blockentity.ResonanceCollectorBlockEntity;
import com.moonproject.moonmod.blockentity.MeltdownSirenBlockEntity;
import com.moonproject.moonmod.blockentity.IdentityExtractorBlockEntity;
import com.moonproject.moonmod.blockentity.MephistophelesEngineBlockEntity;
import com.moonproject.moonmod.blockentity.KCorpInjectorBlockEntity;
import com.moonproject.moonmod.blockentity.GoldenBoughShrineBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MoonProjectMod.MODID);

    public static final RegistryObject<BlockEntityType<ExpBurnerBlockEntity>> EXP_BURNER =
            BLOCK_ENTITIES.register("exp_burner", () ->
                    BlockEntityType.Builder.of(ExpBurnerBlockEntity::new, ModBlocks.EXP_BURNER.get()).build(null)
            );

    /** 场谐振能源采集单元的方块实体。 */
    public static final RegistryObject<BlockEntityType<ResonanceCollectorBlockEntity>> RESONANCE_COLLECTOR =
            BLOCK_ENTITIES.register("resonance_collector", () ->
                    BlockEntityType.Builder.of(ResonanceCollectorBlockEntity::new,
                            ModBlocks.RESONANCE_COLLECTOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<ContainmentUnitBlockEntity>> CONTAINMENT_UNIT =
            BLOCK_ENTITIES.register("containment_unit", () ->
                    BlockEntityType.Builder.of(ContainmentUnitBlockEntity::new, ModBlocks.CONTAINMENT_UNIT.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<EnkephalinExtractorBlockEntity>> ENKEPHALIN_EXTRACTOR =
            BLOCK_ENTITIES.register("enkephalin_extractor", () ->
                    BlockEntityType.Builder.of(EnkephalinExtractorBlockEntity::new, ModBlocks.ENKEPHALIN_EXTRACTOR.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<MeltdownSirenBlockEntity>> MELTDOWN_SIREN =
            BLOCK_ENTITIES.register("meltdown_siren", () ->
                    BlockEntityType.Builder.of(MeltdownSirenBlockEntity::new, ModBlocks.MELTDOWN_SIREN.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<IdentityExtractorBlockEntity>> IDENTITY_EXTRACTOR =
            BLOCK_ENTITIES.register("identity_extractor", () ->
                    BlockEntityType.Builder.of(IdentityExtractorBlockEntity::new, ModBlocks.IDENTITY_EXTRACTOR.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<MephistophelesEngineBlockEntity>> MEPHISTOPHELES_ENGINE =
            BLOCK_ENTITIES.register("mephistopheles_engine", () ->
                    BlockEntityType.Builder.of(MephistophelesEngineBlockEntity::new, ModBlocks.MEPHISTOPHELES_ENGINE.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<KCorpInjectorBlockEntity>> KCORP_INJECTOR =
            BLOCK_ENTITIES.register("kcorp_injector", () ->
                    BlockEntityType.Builder.of(KCorpInjectorBlockEntity::new, ModBlocks.KCORP_INJECTOR.get()).build(null)
            );

    public static final RegistryObject<BlockEntityType<GoldenBoughShrineBlockEntity>> GOLDEN_BOUGH_SHRINE =
            BLOCK_ENTITIES.register("golden_bough_shrine", () ->
                    BlockEntityType.Builder.of(GoldenBoughShrineBlockEntity::new, ModBlocks.GOLDEN_BOUGH_SHRINE.get()).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
