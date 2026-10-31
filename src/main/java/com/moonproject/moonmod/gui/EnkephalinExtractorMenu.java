package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.blockentity.EnkephalinExtractorBlockEntity;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

/**
 * 脑啡肽提取终端容器菜单：
 * 提供催化剂投入槽、脑啡肽产物槽与提取工作进度同步。
 */
public class EnkephalinExtractorMenu extends AbstractContainerMenu {
    private final EnkephalinExtractorBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess levelAccess;

    public EnkephalinExtractorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public EnkephalinExtractorMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.ENKEPHALIN_EXTRACTOR_MENU.get(), containerId);
        this.blockEntity = (EnkephalinExtractorBlockEntity) entity;
        this.data = data;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        addDataSlots(data);

        if (this.blockEntity != null) {
            this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                // 槽位 0: 催化剂 (x=44, y=38)
                addSlot(new SlotItemHandler(handler, 0, 44, 38));
                // 槽位 1: 脑啡肽产物 (x=116, y=38)
                addSlot(new SlotItemHandler(handler, 1, 116, 38) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            });
        }

        // 玩家背包 (3x9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 玩家快捷栏 (1x9)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    public boolean isWorking() {
        return this.data.get(2) != 0;
    }

    public boolean hasAdjacent() {
        return this.data.get(3) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == 0 || index == 1) {
                if (!this.moveItemStackTo(stackInSlot, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (EnkephalinExtractorBlockEntity.isCatalyst(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, this.blockEntity.getBlockState().getBlock());
    }
}
