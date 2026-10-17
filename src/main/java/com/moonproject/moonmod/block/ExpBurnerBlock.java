package com.moonproject.moonmod.block;

import com.moonproject.moonmod.blockentity.ExpBurnerBlockEntity;
import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ExpBurnerBlock extends BaseEntityBlock {

    public ExpBurnerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // 渲染标准方块外观
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExpBurnerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 只在服务端执行心跳逻辑（燃烧、转化经验）
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.EXP_BURNER.get(), ExpBurnerBlockEntity::serverTick);
    }

    // 核心：玩家右键点击方块时触发
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) { // 必须在服务端打开
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ExpBurnerBlockEntity burner) {
                // 通过 Forge 的网络工具打开菜单界面并同步方块坐标
                NetworkHooks.openScreen((ServerPlayer) player, burner, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // 方块被挖掉时，把里面没烧完的燃料吐出来，防止吞东西
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
}