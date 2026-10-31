package com.moonproject.moonmod.worldgen.structure;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, MoonProjectMod.MODID);

    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, MoonProjectMod.MODID);

    // 边狱支部自然生成结构类型
    public static final RegistryObject<StructureType<LimbusBranchStructure>> LIMBUS_BRANCH =
            STRUCTURE_TYPES.register("limbus_branch", () -> () -> LimbusBranchStructure.CODEC);

    // 结构组件切片
    public static final RegistryObject<StructurePieceType> LIMBUS_BRANCH_PIECE =
            PIECE_TYPES.register("limbus_branch_piece", () -> LimbusBranchPiece::new);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        PIECE_TYPES.register(eventBus);
    }
}
