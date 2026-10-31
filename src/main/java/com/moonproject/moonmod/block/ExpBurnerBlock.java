package com.moonproject.moonmod.block;

import com.moonproject.moonmod.blockentity.ExpBurnerBlockEntity;
import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * 蓝色烧书熔炉（焚书熔炉）：
 * 焚烧书籍、附魔书或普通燃料，提炼并存储经验值。
 * 具备面向朝向、点火发光、青蓝火焰粒子和专属科技 UI。
 */
public class ExpBurnerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ExpBurnerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
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
        return new ExpBurnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.EXP_BURNER.get(), ExpBurnerBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ExpBurnerBlockEntity burner) {
                // 如果玩家潜行且熔炉中有经验，可直接右键快速提取
                if (player.isShiftKeyDown() && burner.getStoredExp() > 0) {
                    burner.extractExp(player);
                } else {
                    NetworkHooks.openScreen((ServerPlayer) player, burner, pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ExpBurnerBlockEntity burner) {
                burner.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(0));
                });
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        double x = (double) pos.getX() + 0.5D;
        double y = (double) pos.getY();
        double z = (double) pos.getZ() + 0.5D;

        // 间歇播放熔炉燃烧噼啪声
        if (random.nextDouble() < 0.1D) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }

        // 顶面投入口冒烟雾
        if (random.nextDouble() < 0.35D) {
            level.addParticle(ParticleTypes.SMOKE, x, y + 1.05D, z, 0.0D, 0.04D, 0.0D);
        }

        // 正面炉口处冒青蓝灵魂火焰粒子与烟雾
        Direction direction = state.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double offset = 0.52D;
        double horizontalJitter = random.nextDouble() * 0.4D - 0.2D;
        double px = axis == Direction.Axis.X ? (double) direction.getStepX() * offset : horizontalJitter;
        double py = random.nextDouble() * 5.0D / 16.0D + 0.15D;
        double pz = axis == Direction.Axis.Z ? (double) direction.getStepZ() * offset : horizontalJitter;

        level.addParticle(ParticleTypes.SMOKE, x + px, y + py, z + pz, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + px, y + py, z + pz, 0.0D, 0.01D, 0.0D);
    }
}
