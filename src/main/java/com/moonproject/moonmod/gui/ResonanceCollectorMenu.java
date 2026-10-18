package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.blockentity.ResonanceCollectorBlockEntity;
import com.moonproject.moonmod.init.ModBlocks;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 场谐振能源采集单元的容器。
 *
 * <p>方块槽位：0 = 异想体核心（中央），1 = 脑啡肽产出（右侧），2 = 异想体饰品（底部）。
 * 之后接玩家背包与快捷栏。</p>
 */
public class ResonanceCollectorMenu extends AbstractContainerMenu {

    public static final int BLOCK_SLOTS = 3;

    private final BlockEntity blockEntity;

    /** 客户端/服务端通用构造：从网络包读方块坐标。 */
    public ResonanceCollectorMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ResonanceCollectorMenu(int id, Inventory playerInv, BlockEntity be) {
        super(ModMenus.RESONANCE_COLLECTOR_MENU.get(), id);
        this.blockEntity = be;
        SimpleContainer container = be instanceof ResonanceCollectorBlockEntity collector
                ? collector.getItems() : new SimpleContainer(BLOCK_SLOTS);

        // 方块槽位（坐标按 GUI 底图估计，Screen 里可再微调）
        this.addSlot(new Slot(container, 0, 78, 59));   // 中央：异想体核心
        this.addSlot(new Slot(container, 1, 148, 46));   // 右侧：脑啡肽
        this.addSlot(new Slot(container, 2, 148, 99));  // 底部：饰品

        // 玩家背包 27 格
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 156 + row * 18));
            }
        }
        // 快捷栏 9 格
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 216));
        }
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    /** 界面上的四个工作按钮会调用这里（id = 0..3）。 */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (blockEntity instanceof com.moonproject.moonmod.blockentity.ResonanceCollectorBlockEntity collector) {
            return collector.startWork(id);
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < BLOCK_SLOTS) {
                if (!this.moveItemStackTo(stack, BLOCK_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, BLOCK_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), blockEntity.getBlockPos()),
                player, ModBlocks.RESONANCE_COLLECTOR.get());
    }
}