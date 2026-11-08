package com.moonproject.moonmod.ai.npc.task;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class HarvestTask implements INpcTask {
    private BlockPos targetCropPos = null;

    @Override
    public String getTaskId() {
        return "harvest";
    }

    @Override
    public String getDisplayName() {
        return "农林采掘";
    }

    @Override
    public String getIcon() {
        return "🪓";
    }

    @Override
    public boolean canExecute(AiPlayerNpcEntity npc) {
        return true;
    }

    @Override
    public void onStart(AiPlayerNpcEntity npc) {
        npc.showSpeechBubble("农林勤劳模式启动！让我来收割物资~", 80);
        npc.triggerAnimation(AiPlayerNpcEntity.ANIM_HAPPY, 45);
    }

    @Override
    public void onTick(AiPlayerNpcEntity npc) {
        if (!(npc.level() instanceof ServerLevel serverLevel)) return;

        // 1. 优先吸取附近 6 格掉落在地上的物资到随身背包
        List<ItemEntity> droppedItems = serverLevel.getEntitiesOfClass(ItemEntity.class, npc.getBoundingBox().inflate(6.0D), ItemEntity::isAlive);
        if (!droppedItems.isEmpty()) {
            ItemEntity itemEntity = droppedItems.get(0);
            npc.getNavigation().moveTo(itemEntity, 1.25D);
            if (npc.distanceToSqr(itemEntity) <= 2.5D) {
                ItemStack remaining = npc.getNpcInventory().addItemToBackpack(itemEntity.getItem());
                if (remaining.isEmpty()) {
                    itemEntity.discard();
                    npc.showSpeechBubble("拾取放入了背包！", 40);
                } else {
                    itemEntity.setItem(remaining);
                }
            }
            return;
        }

        // 2. 搜索周围成熟农作物
        if (targetCropPos == null || npc.tickCount % 60 == 0) {
            targetCropPos = findNearestMatureCrop(serverLevel, npc.blockPosition(), 8);
        }

        if (targetCropPos != null) {
            npc.getNavigation().moveTo(targetCropPos.getX(), targetCropPos.getY(), targetCropPos.getZ(), 1.2D);
            if (npc.blockPosition().closerThan(targetCropPos, 2.5D)) {
                BlockState state = serverLevel.getBlockState(targetCropPos);
                if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                    List<ItemStack> drops = Block.getDrops(state, serverLevel, targetCropPos, null);
                    serverLevel.destroyBlock(targetCropPos, false);
                    serverLevel.setBlock(targetCropPos, crop.getStateForAge(0), 3); // 自动补种

                    for (ItemStack drop : drops) {
                        npc.getNpcInventory().addItemToBackpack(drop);
                    }
                    npc.showSpeechBubble("丰收咯！已自动补种！", 60);
                    targetCropPos = null;
                }
            }
        }
    }

    private BlockPos findNearestMatureCrop(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 2, radius))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                return pos.immutable();
            }
        }
        return null;
    }

    @Override
    public void onStop(AiPlayerNpcEntity npc) {
        npc.getNavigation().stop();
        targetCropPos = null;
    }

    @Override
    public boolean isFinished(AiPlayerNpcEntity npc) {
        return false;
    }
}
