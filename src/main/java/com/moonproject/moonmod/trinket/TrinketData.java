package com.moonproject.moonmod.trinket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** {@link ITrinketData} 的默认实现（挂在玩家身上）。 */
public class TrinketData implements ITrinketData {

    private final ItemStack[] slots = new ItemStack[SLOTS];

    public TrinketData() {
        for (int i = 0; i < SLOTS; i++) {
            slots[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack getTrinket(int slot) {
        return slot >= 0 && slot < SLOTS ? slots[slot] : ItemStack.EMPTY;
    }

    @Override
    public void setTrinket(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SLOTS) {
            slots[slot] = stack == null ? ItemStack.EMPTY : stack;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        for (int i = 0; i < SLOTS; i++) {
            String key = "Trinket" + i;
            slots[i] = tag.contains(key) ? ItemStack.of(tag.getCompound(key)) : ItemStack.EMPTY;
        }
    }

    @Override
    public void save(CompoundTag tag) {
        for (int i = 0; i < SLOTS; i++) {
            if (!slots[i].isEmpty()) {
                CompoundTag c = new CompoundTag();
                slots[i].save(c);
                tag.put("Trinket" + i, c);
            }
        }
    }
}