package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 场谐振能源采集单元的方块实体。
 *
 * <p>槽位：
 * <ul>
 *   <li>0：异想体核心（不消耗）</li>
 *   <li>1：脑啡肽产出</li>
 *   <li>2：异想体饰品产出</li>
 * </ul>
 * 工作方式与进度存在 NBT 里，GUI 实现后由界面驱动。</p>
 */
public class ResonanceCollectorBlockEntity extends BlockEntity implements MenuProvider {

    /** 槽位数量。 */
    public static final int SLOTS = 3;
    /** 工作总时长：35 秒。 */
    public static final int WORK_TOTAL = 20 * 35;

    private final SimpleContainer items = new SimpleContainer(SLOTS);

    /** 当前工作方式：-1 = 未工作，0 = 压迫，1 = 沟通，2 = 洞察，3 = 本能。 */
    private int workType = -1;
    /** 已进行的 tick 数。 */
    private int workProgress = 0;

    public ResonanceCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_COLLECTOR.get(), pos, state);
    }

    public SimpleContainer getItems() {
        return items;
    }

    public int getWorkType() {
        return workType;
    }

    public int getWorkProgress() {
        return workProgress;
    }

    public boolean isWorking() {
        return workType >= 0;
    }

    /** 开始一次工作。 */
    public void startWork(int type) {
        this.workType = type;
        this.workProgress = 0;
        this.setChanged();
    }

    /** 每 tick 推进；返回 true 表示本次工作完成。 */
    public boolean tickWork() {
        if (!isWorking()) {
            return false;
        }
        workProgress++;
        if (workProgress >= WORK_TOTAL) {
            workType = -1;
            workProgress = 0;
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        // SimpleContainer 没有 save/load，逐槽手动序列化
        for (int i = 0; i < SLOTS; i++) {
            net.minecraft.world.item.ItemStack stack = items.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag slot = new CompoundTag();
                stack.save(slot);
                tag.put("Slot" + i, slot);
            }
        }
        tag.putInt("WorkType", workType);
        tag.putInt("WorkProgress", workProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < SLOTS; i++) {
            String key = "Slot" + i;
            if (tag.contains(key)) {
                items.setItem(i, net.minecraft.world.item.ItemStack.of(tag.getCompound(key)));
            } else {
                items.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
            }
        }
        workType = tag.getInt("WorkType");
        workProgress = tag.getInt("WorkProgress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.resonance_collector");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, net.minecraft.world.entity.player.Inventory inv, Player player) {
        // 第 2 步接 Menu；现在返回 null 不影响编译
        return null;
    }

    /** 供未来 Menu 使用。 */
    public Container asContainer() {
        return items;
    }
}