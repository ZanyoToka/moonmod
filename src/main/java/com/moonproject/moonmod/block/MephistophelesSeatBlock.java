package com.moonproject.moonmod.block;

import com.moonproject.moonmod.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 边狱巴士特色座椅 —— 梅菲斯特客席 (Mephistopheles Seat - V2 增强版)。
 * <p>全面优化模型体量与乘坐坐标对齐，彻底消除悬空和偏前错位。
 * 支持 4 个水平朝向，右键直接乘坐，享受理智平复与车厢休憩效果。</p>
 */
public class MephistophelesSeatBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // 严谨匹配 V2 豪华高背客车座椅的体素碰撞箱
    private static final VoxelShape SEAT_BASE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            SEAT_BASE,
            Block.box(1.0D, 7.0D, 11.0D, 15.0D, 27.0D, 15.0D), // 靠背在南(+Z)
            Block.box(0.0D, 6.0D, 2.0D, 2.0D, 11.5D, 13.0D),  // 左扶手
            Block.box(14.0D, 6.0D, 2.0D, 16.0D, 11.5D, 13.0D) // 右扶手
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            SEAT_BASE,
            Block.box(1.0D, 7.0D, 1.0D, 15.0D, 27.0D, 5.0D),  // 靠背在北(-Z)
            Block.box(0.0D, 6.0D, 3.0D, 2.0D, 11.5D, 14.0D),
            Block.box(14.0D, 6.0D, 3.0D, 16.0D, 11.5D, 14.0D)
    );

    private static final VoxelShape SHAPE_WEST = Shapes.or(
            SEAT_BASE,
            Block.box(11.0D, 7.0D, 1.0D, 15.0D, 27.0D, 15.0D), // 靠背在东(+X)
            Block.box(3.0D, 6.0D, 0.0D, 14.0D, 11.5D, 2.0D),
            Block.box(3.0D, 6.0D, 14.0D, 14.0D, 11.5D, 16.0D)
    );

    private static final VoxelShape SHAPE_EAST = Shapes.or(
            SEAT_BASE,
            Block.box(1.0D, 7.0D, 1.0D, 5.0D, 27.0D, 15.0D),  // 靠背在西(-X)
            Block.box(2.0D, 6.0D, 0.0D, 13.0D, 11.5D, 2.0D),
            Block.box(2.0D, 6.0D, 14.0D, 13.0D, 11.5D, 16.0D)
    );

    public MephistophelesSeatBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 放置时让椅子正对玩家
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // 检查是否已有存活的骑乘实体
        AABB box = new AABB(pos);
        List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, box);

        SeatEntity seat = null;
        for (SeatEntity existingSeat : seats) {
            if (existingSeat.isAlive()) {
                if (!existingSeat.getPassengers().isEmpty()) {
                    player.displayClientMessage(Component.literal("§c该座位已有乘客。"), true);
                    return InteractionResult.CONSUME;
                }
                seat = existingSeat;
                break;
            }
        }

        Direction facing = state.getValue(FACING);
        float yaw = facing.toYRot();

        // 精确计算坐垫中心与靠背贴合点：
        // 坐垫表面高度为 6.5/16 像素 = 0.40625D，实体基准设在 0.38D
        double targetY = pos.getY() + 0.38D;
        double targetX = pos.getX() + 0.5D;
        double targetZ = pos.getZ() + 0.5D;

        // 根据朝向微调水平位置，让背部自然紧贴靠背
        switch (facing) {
            case NORTH -> targetZ += 0.08D; // 靠背在南，向南微调 1.3 像素贴背
            case SOUTH -> targetZ -= 0.08D; // 靠背在北，向北微调
            case WEST  -> targetX += 0.08D; // 靠背在东，向东微调
            case EAST  -> targetX -= 0.08D; // 靠背在西，向西微调
        }

        if (seat == null) {
            seat = new SeatEntity(level, pos, targetX, targetY, targetZ, yaw);
            level.addFreshEntity(seat);
        } else {
            seat.setPos(targetX, targetY, targetZ);
            seat.setYRot(yaw);
            seat.setYHeadRot(yaw);
        }

        // 让玩家面朝椅子前方并骑乘
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.startRiding(seat, true);

        // 播放入座音效与车厢沉浸提示
        level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.BLOCKS, 1.0F, 0.9F);
        player.displayClientMessage(
                Component.literal("§c[梅菲斯特客席] §f“滴答……车厢引擎平稳运转，在旅途中稍作歇息。”"),
                true
        );

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            AABB box = new AABB(pos);
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, box);
            for (SeatEntity seat : seats) {
                seat.ejectPassengers();
                seat.discard();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
