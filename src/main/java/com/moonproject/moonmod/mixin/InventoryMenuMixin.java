package com.moonproject.moonmod.mixin;

import com.moonproject.moonmod.trinket.ITrinketData;
import com.moonproject.moonmod.trinket.TrinketProvider;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 往原版「背包/角色」界面里插一个饰品槽。
 *
 * <p>位置在装备栏右侧，用原版贴图区域，不额外加贴图。</p>
 */
@Mixin(InventoryMenu.class)
public class InventoryMenuMixin {

    /** 玩家实例（从构造函数参数里拿）。 */
    private static Player moonproject$player;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void moonproject$addTrinketSlot(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        moonproject$player = owner;
        InventoryMenu self = (InventoryMenu) (Object) this;

        // 用一个只读写 capability 的 Container 包一层
        Container trinketContainer = new Container() {
            @Override
            public int getContainerSize() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return TrinketProvider.get(owner).getTrinket(ITrinketData.SLOT_HEAD).isEmpty();
            }

            @Override
            public ItemStack getItem(int slot) {
                return TrinketProvider.get(owner).getTrinket(ITrinketData.SLOT_HEAD);
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack cur = getItem(0);
                if (cur.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                ItemStack out = cur.split(amount);
                TrinketProvider.get(owner).setTrinket(ITrinketData.SLOT_HEAD, cur.isEmpty() ? ItemStack.EMPTY : cur);
                return out;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                ItemStack cur = getItem(0);
                TrinketProvider.get(owner).setTrinket(ITrinketData.SLOT_HEAD, ItemStack.EMPTY);
                return cur;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                TrinketProvider.get(owner).setTrinket(ITrinketData.SLOT_HEAD, stack);
            }

            @Override
            public void setChanged() {
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }

            @Override
            public void clearContent() {
                TrinketProvider.get(owner).setTrinket(ITrinketData.SLOT_HEAD, ItemStack.EMPTY);
            }
        };

        // 位置：装备栏右侧（原版 5 格装备栏在 x=8,y=8~58）
        self.slots.add(new Slot(trinketContainer, 0, 77, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof com.moonproject.moonmod.item.AbnormalityTrinketItem;
            }
        });
    }
}