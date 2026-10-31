package com.moonproject.moonmod.worldgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.Optional;

/**
 * 边狱支部自然生成结构：
 * 包含地表倾斜的梅菲斯特客车残骸，以及深埋于地底的三层收容与共鸣圣殿。
 */
public class LimbusBranchStructure extends Structure {
    public static final Codec<LimbusBranchStructure> CODEC = simpleCodec(LimbusBranchStructure::new);

    public LimbusBranchStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // 获取当前区块中心的表面地表高度
        BlockPos centerPos = context.chunkPos().getMiddleBlockPosition(0);
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerPos.getX(),
                centerPos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        );

        // 如果生成高度过低（如接近基岩或虚空），跳过生成
        if (surfaceY < 40) {
            return Optional.empty();
        }

        // 以地表高度为基准点，整个建筑向下延展 25 格，向上露出 6 格（半截车头）
        BlockPos origin = new BlockPos(centerPos.getX(), surfaceY, centerPos.getZ());

        return Optional.of(new GenerationStub(origin, builder -> {
            BoundingBox box = new BoundingBox(
                    origin.getX() - 12, origin.getY() - 25, origin.getZ() - 12,
                    origin.getX() + 12, origin.getY() + 8, origin.getZ() + 12
            );
            builder.addPiece(new LimbusBranchPiece(origin, box));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.LIMBUS_BRANCH.get();
    }
}
