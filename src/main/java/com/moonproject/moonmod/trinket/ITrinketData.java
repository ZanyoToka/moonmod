package com.moonproject.moonmod.trinket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * 玩家身上的「异想体饰品」数据。
 *
 * <p>目前只有一个头饰槽位；后续可以扩展成项链/戒指等多个槽。</p>
 */
@AutoRegisterCapability
public interface ITrinketData {

    /** 槽位数量。 */
    int SLOTS = 4;
    /** 头饰槽。 */
    int SLOT_HEAD = 0;
    /** 项链槽。 */
    int SLOT_NECK = 1;
    /** 戒指槽 1。 */
    int SLOT_RING1 = 2;
    /** 戒指槽 2。 */
    int SLOT_RING2 = 3;

    /** 读取某个槽位的饰品。 */
    ItemStack getTrinket(int slot);

    /** 放入/取出某个槽位的饰品。 */
    void setTrinket(int slot, ItemStack stack);

    /** 读存档。 */
    void load(CompoundTag tag);

    /** 写存档。 */
    void save(CompoundTag tag);
}