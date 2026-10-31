package com.moonproject.moonmod.block;

import com.moonproject.moonmod.blockentity.IdentityExtractorBlockEntity;
import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 【人格抽取共鸣舱】 (Identity Extractor)
 * <p>边狱公司奇点共鸣设施。可投入异想体碎片、E.G.O核心、附魔之瓶或镜面水晶，
 * 跨越镜面世界轴抽取高阶人格卡与书页，伴随镜面碎裂与翻硬币声音。</p>
 */
public class IdentityExtractorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty EXTRACTING = BooleanProperty.create("extracting");

    public IdentityExtractorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(EXTRACTING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTRACTING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IdentityExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.IDENTITY_EXTRACTOR.get(), IdentityExtractorBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IdentityExtractorBlockEntity extractor)) {
            return InteractionResult.PASS;
        }

        if (extractor.isExtracting()) {
            player.displayClientMessage(
                    Component.literal("§d[人格共鸣舱] §e镜面世界共鸣正在进行中，请等待时空轴稳定……"),
                    true
            );
            return InteractionResult.CONSUME;
        }

        ItemStack held = player.getItemInHand(hand);
        if (IdentityExtractorBlockEntity.isExtractFuel(held)) {
            boolean started = extractor.startExtraction(held);
            if (started) {
                player.displayClientMessage(
                        Component.literal("§d[人格共鸣舱] §a已投入媒介，镜面世界共鸣通道开启！"),
                        true
                );
                return InteractionResult.CONSUME;
            }
        } else {
            player.displayClientMessage(
                    Component.literal("§7[人格共鸣舱] 请手持 §b【异想体碎片】§7、§e【E.G.O核心】§7、§d【紫水晶碎片】§7或§a【经验瓶】§7右键投入抽取。"),
                    true
            );
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IdentityExtractorBlockEntity extractor) {
                extractor.dropInventory();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(EXTRACTING)) {
            double px = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
            double py = pos.getY() + 0.6D + random.nextDouble() * 0.4D;
            double pz = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;

            // 苍白裂痕与深紫雾气
            level.addParticle(ParticleTypes.PORTAL, px, py, pz, (random.nextDouble() - 0.5D) * 0.5D, (random.nextDouble() - 0.5D) * 0.5D, (random.nextDouble() - 0.5D) * 0.5D);
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 0.0D, 0.02D, 0.0D);
            }
        }
    }
}
