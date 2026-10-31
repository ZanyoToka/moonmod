package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.blockentity.MephistophelesEngineBlockEntity;
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
 * 梅菲斯特引擎核心容器菜单：
 * 提供生化残渣喂食槽位、燃料燃烧时长同步与客席协同状态。
 */
public class MephistophelesEngineMenu extends AbstractContainerMenu {
    private final MephistophelesEngineBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess levelAccess;

    public MephistophelesEngineMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public MephistophelesEngineMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.MEPHISTOPHELES_ENGINE_MENU.get(), containerId);
        this.blockEntity = (MephistophelesEngineBlockEntity) entity;
        this.data = data;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        addDataSlots(data);

        if (this.blockEntity != null) {
            this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                // 生物柴油残渣槽 (x=80, y=38)
                addSlot(new SlotItemHandler(handler, 0, 80, 38) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return MephistophelesEngineBlockEntity.getBioFuelValue(stack) > 0;
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

    public int getFuelTicks() {
        return this.data.get(0);
    }

    public int getMaxFuelTicks() {
        return this.data.get(1);
    }

    public int getConnectedSeats() {
        return this.data.get(2);
    }

    public boolean isLit() {
        return this.data.get(3) != 0;
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
                if (MephistophelesEngineBlockEntity.getBioFuelValue(stackInSlot) > 0) {
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
