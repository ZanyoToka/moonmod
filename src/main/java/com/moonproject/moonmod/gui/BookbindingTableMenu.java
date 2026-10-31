package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.init.ModBlocks;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

/**
 * 总类层装订台容器菜单：
 * 基于 3x3 书籍装订矩阵，连接废墟图书馆知识体系。
 */
public class BookbindingTableMenu extends CraftingMenu {
    private final ContainerLevelAccess access;

    public BookbindingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, ContainerLevelAccess.create(playerInventory.player.level(), extraData.readBlockPos()));
    }

    public BookbindingTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(containerId, playerInventory, access);
        this.access = access;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.BOOKBINDING_TABLE.get());
    }
}
