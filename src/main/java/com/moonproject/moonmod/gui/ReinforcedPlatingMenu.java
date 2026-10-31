package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.init.ModBlocks;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * 都市重装防爆壁监控菜单：
 * 同步护壁结构强度、抗爆系数与邻接收容室加固读数。
 */
public class ReinforcedPlatingMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess levelAccess;
    private final ContainerData data;

    public ReinforcedPlatingMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, ContainerLevelAccess.create(inv.player.level(), extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public ReinforcedPlatingMenu(int containerId, Inventory inv, ContainerLevelAccess access, ContainerData data) {
        super(ModMenus.REINFORCED_PLATING_MENU.get(), containerId);
        this.levelAccess = access;
        this.data = data;

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

    public int getIntegrity() {
        return 100;
    }

    public int getBlastResistance() {
        return 1200;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, ModBlocks.REINFORCED_PLATING.get());
    }
}
