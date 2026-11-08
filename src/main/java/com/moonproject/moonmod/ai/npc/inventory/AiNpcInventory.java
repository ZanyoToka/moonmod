package com.moonproject.moonmod.ai.npc.inventory;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * AI NPC 专属独立随身行囊容器 (借鉴 Touhou Little Maid 的独立背包设计)
 * 包含：
 * - 4 格盔甲槽位 (头、胸、腿、脚)
 * - 2 格手持槽位 (主手、副手)
 * - 18 格 (2x9) 随身大容量行囊储存空间
 * 自动与实体的原版装备栏双向同步，并支持完整的 NBT 持久化保存与反序列化。
 */
public class AiNpcInventory extends SimpleContainer {

    public static final int SLOT_MAINHAND = 0;
    public static final int SLOT_OFFHAND = 1;
    public static final int SLOT_HEAD = 2;
    public static final int SLOT_CHEST = 3;
    public static final int SLOT_LEGS = 4;
    public static final int SLOT_FEET = 5;
    public static final int BACKPACK_START = 6;
    public static final int BACKPACK_SIZE = 18;
    public static final int TOTAL_SIZE = 24;

    private final AiPlayerNpcEntity npc;
    private boolean syncing = false;

    public AiNpcInventory(AiPlayerNpcEntity npc) {
        super(TOTAL_SIZE);
        this.npc = npc;
        this.addListener(this::onInventoryChanged);
    }

    private void onInventoryChanged(Container container) {
        if (syncing || npc == null) return;
        syncing = true;
        try {
            // 将槽位物品同步到实体的原版装备栏，实时显示手持物与装备外显
            npc.setItemSlot(EquipmentSlot.MAINHAND, getItem(SLOT_MAINHAND));
            npc.setItemSlot(EquipmentSlot.OFFHAND, getItem(SLOT_OFFHAND));
            npc.setItemSlot(EquipmentSlot.HEAD, getItem(SLOT_HEAD));
            npc.setItemSlot(EquipmentSlot.CHEST, getItem(SLOT_CHEST));
            npc.setItemSlot(EquipmentSlot.LEGS, getItem(SLOT_LEGS));
            npc.setItemSlot(EquipmentSlot.FEET, getItem(SLOT_FEET));
        } finally {
            syncing = false;
        }
    }

    /**
     * 将实体原版装备栏同步回容器
     */
    public void syncFromEntity() {
        if (npc == null || syncing) return;
        syncing = true;
        try {
            setItem(SLOT_MAINHAND, npc.getItemBySlot(EquipmentSlot.MAINHAND));
            setItem(SLOT_OFFHAND, npc.getItemBySlot(EquipmentSlot.OFFHAND));
            setItem(SLOT_HEAD, npc.getItemBySlot(EquipmentSlot.HEAD));
            setItem(SLOT_CHEST, npc.getItemBySlot(EquipmentSlot.CHEST));
            setItem(SLOT_LEGS, npc.getItemBySlot(EquipmentSlot.LEGS));
            setItem(SLOT_FEET, npc.getItemBySlot(EquipmentSlot.FEET));
        } finally {
            syncing = false;
        }
    }

    /**
     * 尝试将物品添加进随身行囊 (优先堆叠，找不到则放空槽)
     * @return 无法放入的剩余物品
     */
    public ItemStack addItemToBackpack(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack remaining = stack.copy();

        // 1. 尝试堆叠进已有同种物品槽位
        for (int i = BACKPACK_START; i < TOTAL_SIZE; i++) {
            ItemStack slotStack = getItem(i);
            if (ItemStack.isSameItemSameTags(slotStack, remaining)) {
                int space = slotStack.getMaxStackSize() - slotStack.getCount();
                int transfer = Math.min(space, remaining.getCount());
                if (transfer > 0) {
                    slotStack.grow(transfer);
                    remaining.shrink(transfer);
                    if (remaining.isEmpty()) {
                        setChanged();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        // 2. 放入空槽位
        for (int i = BACKPACK_START; i < TOTAL_SIZE; i++) {
            if (getItem(i).isEmpty()) {
                setItem(i, remaining.copy());
                setChanged();
                return ItemStack.EMPTY;
            }
        }

        setChanged();
        return remaining;
    }

    /**
     * NBT 序列化
     */
    public CompoundTag writeNbt(CompoundTag tag) {
        ListTag list = new ListTag();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                stack.save(itemTag);
                list.add(itemTag);
            }
        }
        tag.put("NpcInventory", list);
        return tag;
    }

    /**
     * NBT 反序列化
     */
    public void readNbt(CompoundTag tag) {
        if (!tag.contains("NpcInventory", 9)) return;
        ListTag list = tag.getList("NpcInventory", 10);
        clearContent();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < getContainerSize()) {
                setItem(slot, ItemStack.of(itemTag));
            }
        }
        syncFromEntity();
    }
}
