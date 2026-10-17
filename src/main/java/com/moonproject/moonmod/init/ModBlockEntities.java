package com.moonproject.moonmod.init;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.blockentity.ExpBurnerBlockEntity;
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

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}