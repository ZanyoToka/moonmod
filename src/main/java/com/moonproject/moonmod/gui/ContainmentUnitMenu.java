package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.blockentity.ContainmentUnitBlockEntity;
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
 * 异想体收容单元容器菜单：
 * 同步收容室内部核心槽位、逆卡巴拉计数、四周防爆加成与警戒状态。
 */
public class ContainmentUnitMenu extends AbstractContainerMenu {
    private final ContainmentUnitBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess levelAccess;

    // 客户端构造器
    public ContainmentUnitMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    // 服务端构造器
    public ContainmentUnitMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.CONTAINMENT_UNIT_MENU.get(), containerId);
        this.blockEntity = (ContainmentUnitBlockEntity) entity;
        this.data = data;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        addDataSlots(data);

        // 核心槽位 (中心位置 x=80, y=42)
        if (this.blockEntity != null) {
            this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                addSlot(new SlotItemHandler(handler, 0, 80, 42));
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

    public int getQlipothCounter() {
        return this.data.get(0);
    }

    public int getMaxQlipoth() {
        return this.data.get(1);
    }

    public int getAdjacentPlatings() {
        return this.data.get(2);
    }

    public boolean isAlert() {
        return this.data.get(3) != 0;
    }

    /**
     * 当前收容对象的显示名（客户端可读：槽位内容由容器同步，方块实体在双方都存在）。
     */
    public String getAbnormalityName() {
        return this.blockEntity == null ? "未装载" : this.blockEntity.getAbnormalityName();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && this.blockEntity != null) {
            // 按钮 0：紧急逆卡巴拉抑制
            this.blockEntity.suppressMeltdown(player);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
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
