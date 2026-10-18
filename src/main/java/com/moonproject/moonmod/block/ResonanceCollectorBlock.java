package com.moonproject.moonmod.block;

import com.moonproject.moonmod.blockentity.ResonanceCollectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * 场谐振能源采集单元：从异想体提取脑啡肽的工作方块。
 *
 * <p>采集等级由数据包标签控制：
 * {@code mineable/pickaxe}（需要镐）、{@code needs_iron_tool}（至少铁镐）。</p>
 */
public class ResonanceCollectorBlock extends BaseEntityBlock {

    public ResonanceCollectorBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(4.0F, 12.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    @Override
    public net.minecraft.world.InteractionResult use(net.minecraft.world.level.block.state.BlockState state,
                                                     net.minecraft.world.level.Level level, BlockPos pos,
                                                     net.minecraft.world.entity.player.Player player,
                                                     net.minecraft.world.InteractionHand hand,
                                                     net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ResonanceCollectorBlockEntity collector) {
                net.minecraftforge.network.NetworkHooks.openScreen(
                        (net.minecraft.server.level.ServerPlayer) player, collector, pos);
            }
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResonanceCollectorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            net.minecraft.world.level.Level level, BlockState state,
            net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return (lvl, pos, st, be) -> {
            if (be instanceof ResonanceCollectorBlockEntity collector) {
                ResonanceCollectorBlockEntity.serverTick(lvl, pos, st, collector);
            }
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** 掉落物里要把容器内容也吐出来。 */
    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ResonanceCollectorBlockEntity collector) {
                for (int i = 0; i < ResonanceCollectorBlockEntity.SLOTS; i++) {
                    net.minecraft.world.item.ItemStack stack = collector.getItems().getItem(i);
                    if (!stack.isEmpty()) {
                        net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}