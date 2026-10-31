package com.moonproject.moonmod.block;

import com.moonproject.moonmod.blockentity.ContainmentUnitBlockEntity;
import com.moonproject.moonmod.blockentity.EnkephalinExtractorBlockEntity;
import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

/**
 * 脑啡肽提取终端 (Enkephalin Extractor Block):
 * 建立在异想体收容室旁的能量抽取设备。
 * 支持放入催化剂（红石、青金石等），从相邻的收容单元中提炼纯净脑啡肽（异想体碎片）。
 */
public class EnkephalinExtractorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    public EnkephalinExtractorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WORKING, false));
    }

    public EnkephalinExtractorBlock() {
        this(BlockBehaviour.Properties.of()
                .strength(4.5F, 30.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(WORKING) ? 10 : 2));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WORKING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WORKING, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnkephalinExtractorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ENKEPHALIN_EXTRACTOR.get(), EnkephalinExtractorBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EnkephalinExtractorBlockEntity extractor) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, extractor, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnkephalinExtractorBlockEntity extractor) {
                extractor.drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(WORKING)) {
            // 工作时喷出青色光辉与微小电火花
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.7;
            double y = pos.getY() + 0.8 + (random.nextDouble() - 0.5) * 0.4;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.7;

            level.addParticle(new DustParticleOptions(new Vector3f(0.1F, 0.9F, 0.95F), 1.0F),
                    x, y, z, 0.0, 0.05, 0.0);

            if (random.nextFloat() < 0.3F) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0, 0.02, 0.0);
            }
        }
    }
}
