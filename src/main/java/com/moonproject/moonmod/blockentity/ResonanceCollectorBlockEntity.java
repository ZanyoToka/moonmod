package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 场谐振能源采集单元的方块实体。
 *
 * <p>槽位：0 = 异想体核心（不消耗），1 = 脑啡肽产出，2 = 异想体饰品产出。</p>
 *
 * <p>工作流程：点界面上的四个按钮之一 → 开始 35 秒工作 → 完成后按工作方式产出
 * 脑啡肽放进槽位 1；另有 3% 概率产出饰品、1% 概率生成异想体（后续接入）。</p>
 */
public class ResonanceCollectorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_CORE = 0;
    public static final int SLOT_ENKEPHALIN = 1;
    public static final int SLOT_TRINKET = 2;
    public static final int SLOTS = 3;

    /** 工作总时长：35 秒。 */
    public static final int WORK_TOTAL = 20 * 35;

    /** 工作方式：0=压迫，1=沟通，2=洞察，3=本能。 */
    public static final String[] WORK_NAMES = {"压迫", "沟通", "洞察", "本能"};

    private final SimpleContainer items = new SimpleContainer(SLOTS);

    private int workType = -1;
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

    /** 开始工作：核心槽为空时不能开始。 */
    public boolean startWork(int type) {
        if (type < 0 || type > 3 || isWorking()) {
            return false;
        }
        if (items.getItem(SLOT_CORE).isEmpty()) {
            return false;
        }
        this.workType = type;
        this.workProgress = 0;
        setChanged();
        return true;
    }

    /** 每 tick 由 {@link #serverTick} 调用。 */
    private void tickWork(Level level) {
        if (!isWorking()) {
            return;
        }
        workProgress++;
        if (workProgress >= WORK_TOTAL) {
            int type = workType;
            workType = -1;
            workProgress = 0;
            finishWork(level, type);
            setChanged();
        }
    }

    /** 工作结束结算：产出脑啡肽；饰品/异想体概率后续接入。 */
    private void finishWork(Level level, int type) {
        // 各工作方式对应的基础产出（之后可按核心的倾向数据调整）
        int amount = 1 + type;
        ItemStack out = items.getItem(SLOT_ENKEPHALIN);
        if (out.isEmpty()) {
            items.setItem(SLOT_ENKEPHALIN, new ItemStack(
                    com.moonproject.moonmod.item.ModItems.ENKEPHALIN.get(), amount));
        } else if (out.is(com.moonproject.moonmod.item.ModItems.ENKEPHALIN.get())) {
            out.grow(amount);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonanceCollectorBlockEntity be) {
        be.tickWork(level);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.getItem(i);
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
            items.setItem(i, tag.contains(key) ? ItemStack.of(tag.getCompound(key)) : ItemStack.EMPTY);
        }
        workType = tag.getInt("WorkType");
        workProgress = tag.getInt("WorkProgress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.resonance_collector");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new com.moonproject.moonmod.gui.ResonanceCollectorMenu(windowId, inv, this);
    }
}