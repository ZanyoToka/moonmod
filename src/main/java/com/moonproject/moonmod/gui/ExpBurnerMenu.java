package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.blockentity.ExpBurnerBlockEntity;
import com.moonproject.moonmod.init.ModBlocks;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.SlotItemHandler;

public class ExpBurnerMenu extends AbstractContainerMenu {
    private final ExpBurnerBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess levelAccess;

    // 客户端调用的构造器
    public ExpBurnerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    // 服务端调用的构造器
    public ExpBurnerMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenus.EXP_BURNER_MENU.get(), containerId);
        this.blockEntity = (ExpBurnerBlockEntity) entity;
        this.data = data;
        this.levelAccess = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        // 绑定容器数据同步（0: litTime, 1: maxLitTime, 2: storedExp）
        addDataSlots(data);

        // 1. 添加机器的燃料槽位 (位于 GUI 坐标 x=80, y=36)
        this.blockEntity.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            addSlot(new SlotItemHandler(handler, 0, 56, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
                }
            });
        });

        // 2. 添加玩家主背包 (3x9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // 3. 添加玩家快捷栏 (1x9)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142));
        }
    }

    // 处理客户端点击“提取经验”按钮（通过原版菜单按钮协议）
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && this.blockEntity != null) {
            this.blockEntity.extractExp(player);
            return true;
        }
        return false;
    }

    public int getStoredExp() {
        return this.data.get(2);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    public int getLitProgressScaled(int pixels) {
        int lit = this.data.get(0);
        int max = this.data.get(1);
        if (max == 0) return 0;
        return lit * pixels / max;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();
            if (index == 0) { // 机器燃料槽移到玩家背包
                if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) return ItemStack.EMPTY;
            } else { // 玩家背包移到机器槽位
                if (ForgeHooks.getBurnTime(stackInSlot, RecipeType.SMELTING) > 0) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.levelAccess, player, ModBlocks.EXP_BURNER.get());
    }
}