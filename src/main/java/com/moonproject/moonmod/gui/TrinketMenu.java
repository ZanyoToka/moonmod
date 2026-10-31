package com.moonproject.moonmod.gui;

import com.moonproject.moonmod.init.ModMenus;
import com.moonproject.moonmod.trinket.ITrinketData;
import com.moonproject.moonmod.trinket.TrinketProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 饰品界面：中间一个头饰槽 + 玩家背包。 */
public class TrinketMenu extends AbstractContainerMenu {

    /** 饰品槽下标（本容器内）。 */
    public static final int TRINKET_SLOT = 0;

    private final Player owner;

    public TrinketMenu(int id, Inventory playerInv) {
        super(ModMenus.TRINKET_MENU.get(), id);
        this.owner = playerInv.player;

        // 四个饰品槽：头饰 / 项链 / 戒指 / 戒指
        int[] xs = {44, 80, 116, 152};
        int y = 40;
        String[] slotNames = {"head", "neck", "ring", "ring"};
        for (int i = 0; i < ITrinketData.SLOTS; i++) {
            final int idx = i;
            final String slotType = slotNames[i];
            this.addSlot(new Slot(playerInv, i, xs[i], y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (!(stack.getItem() instanceof com.moonproject.moonmod.item.AbnormalityTrinketItem trinket)) {
                        return false;
                    }
                    return trinket.getSlot().equals(slotType);
                }

                @Override
                public ItemStack getItem() {
                    return TrinketProvider.get(owner).getTrinket(idx);
                }

                @Override
                public void set(ItemStack stack) {
                    TrinketProvider.get(owner).setTrinket(idx, stack);
                }

                @Override
                public void setChanged() {
                    // 数据存在 capability 里
                }
            });
        }

        // 玩家背包 27 格
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 100 + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 158));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == TRINKET_SLOT) {
                if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, TRINKET_SLOT, TRINKET_SLOT + 1, false)) {
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

    public Player getOwner() {
        return owner;
    }
}