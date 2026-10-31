package com.moonproject.moonmod.block;

import com.moonproject.moonmod.blockentity.MeltdownSirenBlockEntity;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkHooks;

/**
 * 脑叶警报广播机 (Meltdown Siren Block):
 * 脑叶公司设施防灾警报广播系统。
 * 在接收到红石信号或附近收容室进入逆卡巴拉崩溃危机（安定度 < 30%）时触发红光警报与防空声浪，
 * 为防暴人员提供速度与伤害增益，并标记曝光附近入侵与逃逸的敌对生物。
 */
public class MeltdownSirenBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    // 专属警报机造型碰撞箱
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public MeltdownSirenBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    public MeltdownSirenBlock() {
        this(BlockBehaviour.Properties.of()
                .strength(3.5F, 12.0F)
                .sound(SoundType.ANVIL)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(ACTIVE) ? 14 : 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean hasSignal = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState().setValue(ACTIVE, hasSignal);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeltdownSirenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.MELTDOWN_SIREN.get(), MeltdownSirenBlockEntity::serverTick);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            BlockEntity be = level.getBlockEntity(pos);
            int meltdowns = 0;
            if (be instanceof MeltdownSirenBlockEntity siren) {
                meltdowns = siren.getNearbyMeltdowns();
            }
            boolean shouldActive = hasSignal || meltdowns > 0;
            if (state.getValue(ACTIVE) != shouldActive) {
                level.setBlock(pos, state.setValue(ACTIVE, shouldActive), 3);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MeltdownSirenBlockEntity siren) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, siren, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) {
            // 警报拉响时，顶部旋转闪烁强烈的红光微粒与报警烟雾
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 1.05;
            double cz = pos.getZ() + 0.5;

            // 红色警报高亮粉尘
            for (int i = 0; i < 3; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double dist = 0.2 + random.nextDouble() * 0.3;
                double px = cx + Math.cos(angle) * dist;
                double pz = cz + Math.sin(angle) * dist;
                level.addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.05F, 0.05F), 1.8F),
                        px, cy, pz, 0.0, 0.1, 0.0);
            }

            if (random.nextFloat() < 0.25F) {
                level.addParticle(ParticleTypes.SMOKE, cx, cy, cz, 0.0, 0.08, 0.0);
            }
        }
    }
}
