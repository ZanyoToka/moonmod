package com.moonproject.moonmod.ai.npc.inventory;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import com.moonproject.moonmod.init.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * AI NPC 专属随身背包与装备栏 Container 菜单
 * 支持：
 * - 4 格盔甲槽 (带原版盔甲类型限制与防具判定)
 * - 2 格手持武器槽 (主手/副手)
 * - 18 格 (2x9) 随身大容量行囊
 * - 玩家 3x9 背包与 1x9 快捷栏
 * - 完备的 Shift 快速移动 (quickMoveStack)
 */
public class NpcInventoryMenu extends AbstractContainerMenu {

    private final AiPlayerNpcEntity npc;
    private final AiNpcInventory npcInventory;

    public NpcInventoryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getEntityFromBuf(playerInventory, extraData));
    }

    private static AiPlayerNpcEntity getEntityFromBuf(Inventory playerInventory, FriendlyByteBuf buf) {
        if (buf != null && playerInventory != null && playerInventory.player != null) {
            int entityId = buf.readVarInt();
            if (playerInventory.player.level().getEntity(entityId) instanceof AiPlayerNpcEntity npcEntity) {
                return npcEntity;
            }
        }
        return null;
    }

    public NpcInventoryMenu(int containerId, Inventory playerInventory, AiPlayerNpcEntity npc) {
        super(ModMenus.NPC_INVENTORY_MENU.get(), containerId);
        this.npc = npc;
        this.npcInventory = (npc != null) ? npc.getNpcInventory() : new AiNpcInventory(null);
        this.npcInventory.syncFromEntity();

        // 1. NPC 盔甲槽位 (0..3: 头、胸、腿、脚)
        EquipmentSlot[] armorSlots = new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        for (int i = 0; i < 4; i++) {
            final EquipmentSlot slotType = armorSlots[i];
            int containerSlot = AiNpcInventory.SLOT_HEAD + i;
            this.addSlot(new Slot(npcInventory, containerSlot, 8, 18 + i * 18) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == slotType;
                }
            });
        }

        // 2. NPC 手持物槽位 (4: 主手, 5: 副手)
        this.addSlot(new Slot(npcInventory, AiNpcInventory.SLOT_MAINHAND, 80, 54));
        this.addSlot(new Slot(npcInventory, AiNpcInventory.SLOT_OFFHAND, 80, 72));

        // 3. NPC 随身行囊 18 格 (2x9, 从 containerSlot 6 开始, 菜单槽位 6..23)
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                int containerSlot = AiNpcInventory.BACKPACK_START + row * 9 + col;
                this.addSlot(new Slot(npcInventory, containerSlot, 105 + col * 18, 18 + row * 18));
            }
        }

        // 4. 玩家 3x9 背包槽位 (菜单槽位 24..50)
        int playerInvY = 104;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 105 + col * 18, playerInvY + row * 18));
            }
        }

        // 5. 玩家 1x9 快捷栏槽位 (菜单槽位 51..59)
        int hotbarY = 162;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 105 + col * 18, hotbarY));
        }
    }

    public AiPlayerNpcEntity getNpc() {
        return npc;
    }

    @Override
    public boolean stillValid(Player player) {
        if (npc == null || !npc.isAlive()) return false;
        return player.distanceToSqr(npc) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 如果点击的是 NPC 槽位 (0..23)，尝试快速移入玩家背包 (24..60)
            if (index < 24) {
                if (!this.moveItemStackTo(slotStack, 24, 60, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 如果点击的是玩家背包 (24..60)
                // 1. 先尝试放入盔甲槽 (0..3)
                if (slotStack.getItem() instanceof ArmorItem armor) {
                    int armorSlotIndex = switch (armor.getEquipmentSlot()) {
                        case HEAD -> 0;
                        case CHEST -> 1;
                        case LEGS -> 2;
                        case FEET -> 3;
                        default -> -1;
                    };
                    if (armorSlotIndex >= 0 && !this.slots.get(armorSlotIndex).hasItem()) {
                        if (this.moveItemStackTo(slotStack, armorSlotIndex, armorSlotIndex + 1, false)) {
                            slot.onTake(player, slotStack);
                            return itemstack;
                        }
                    }
                }
                // 2. 尝试放入 NPC 随身行囊 (6..24)
                if (!slotStack.isEmpty() && !this.moveItemStackTo(slotStack, AiNpcInventory.BACKPACK_START, 24, false)) {
                    // 3. 在玩家自身背包与快捷栏之间切换
                    if (index < 51) {
                        if (!this.moveItemStackTo(slotStack, 51, 60, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, 24, 51, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }
}
