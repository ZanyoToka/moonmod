package com.moonproject.moonmod.block;

import com.moonproject.moonmod.blockentity.ContainmentUnitBlockEntity;
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
 * 异想体收容单元 (Containment Unit Block):
 * 脑叶公司的核心收容设备。
 * 支持放置/取出异想体核心，维持逆卡巴拉计数，支持红石比较器输出当前安定度。
 */
public class ContainmentUnitBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** 警报状态：安定度低于 30% 时触发视觉红光与警报粒子 */
    public static final BooleanProperty ALERT = BooleanProperty.create("alert");

    public ContainmentUnitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ALERT, false));
    }

    public ContainmentUnitBlock() {
        this(BlockBehaviour.Properties.of()
                .strength(6.0F, 120.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(ALERT) ? 9 : 3));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ALERT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ALERT, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ContainmentUnitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CONTAINMENT_UNIT.get(), ContainmentUnitBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ContainmentUnitBlockEntity containmentUnit) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, containmentUnit, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ContainmentUnitBlockEntity unit) {
            // 安定度 0 ~ 100 映射为红石信号 0 ~ 15
            return (int) Math.round((unit.getQlipothCounter() / 100.0) * 15.0);
        }
        return 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ContainmentUnitBlockEntity unit) {
                unit.drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ALERT)) {
            // 警报模式粒子：在收容单元顶部与四周释放红色警告微粒
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
            double y = pos.getY() + 1.05;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
            level.addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 1.2F),
                    x, y, z, 0.0, 0.05, 0.0);
        } else {
            // 正常收容运行微弱科技粒子
            if (random.nextInt(4) == 0) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
                double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
                level.addParticle(ParticleTypes.ENCHANT, x, y, z, 0.0, 0.1, 0.0);
            }
        }
    }
}
