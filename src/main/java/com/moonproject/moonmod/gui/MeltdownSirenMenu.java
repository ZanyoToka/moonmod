package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.blockentity.MeltdownSirenBlockEntity;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 脑叶警报广播机容器菜单：
 * 同步警报广播激活态、周边危险收容室数量、手动鸣笛状态。
 */
public class MeltdownSirenMenu extends AbstractContainerMenu {
    private final MeltdownSirenBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess levelAccess;

    public MeltdownSirenMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public MeltdownSirenMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.MELTDOWN_SIREN_MENU.get(), containerId);
        this.blockEntity = (MeltdownSirenBlockEntity) entity;
        this.data = data;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        addDataSlots(data);

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

    public boolean isAlert() {
        return this.data.get(0) != 0;
    }

    public int getNearbyMeltdowns() {
        return this.data.get(1);
    }

    public boolean isManualOverride() {
        return this.data.get(2) != 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && this.blockEntity != null) {
            // 手动鸣笛切换
            this.blockEntity.toggleManualTrigger(player);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, this.blockEntity.getBlockState().getBlock());
    }
}
