package com.moonproject.moonmod.trinket;

import com.moonproject.moonmod.item.AbnormalityTrinketItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 饰品系统通用工具类，封装对社区 Curios API 的调用。
 *
 * <p>供战斗结算、理智(SP)系统、异想体交互等模块查询玩家当前佩戴的异想体饰品。</p>
 */
public final class TrinketHelper {

    /** 检查当前环境是否加载了 Curios API。 */
    public static boolean isCuriosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    /** 查找实体身上是否佩戴了指定物品类型的饰品。 */
    public static Optional<ItemStack> findFirstTrinket(LivingEntity entity, Item item) {
        if (!isCuriosLoaded() || entity == null || item == null) {
            return Optional.empty();
        }
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .flatMap(handler -> handler.findFirstCurio(item))
                .map(SlotResult::stack);
    }

    /** 按条件查找实体身上佩戴的第一个饰品。 */
    public static Optional<ItemStack> findFirstTrinket(LivingEntity entity, Predicate<ItemStack> predicate) {
        if (!isCuriosLoaded() || entity == null || predicate == null) {
            return Optional.empty();
        }
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .flatMap(handler -> handler.findFirstCurio(predicate))
                .map(SlotResult::stack);
    }

    /** 检查实体是否佩戴了指定异想体核心 coreId 对应的饰品。 */
    public static boolean isWearingCoreTrinket(LivingEntity entity, String coreId) {
        if (coreId == null || !isCuriosLoaded()) {
            return false;
        }
        return findFirstTrinket(entity, stack ->
                stack.getItem() instanceof AbnormalityTrinketItem trinket && coreId.equals(trinket.getCoreId())
        ).isPresent();
    }

    /** 获取实体身上所有已佩戴的饰品列表。 */
    public static List<ItemStack> getEquippedTrinkets(LivingEntity entity) {
        if (!isCuriosLoaded() || entity == null) {
            return Collections.emptyList();
        }
        var handlerOpt = CuriosApi.getCuriosInventory(entity);
        if (!handlerOpt.isPresent()) {
            return Collections.emptyList();
        }
        var handler = handlerOpt.resolve().orElse(null);
        if (handler == null) {
            return Collections.emptyList();
        }

        List<ItemStack> list = new ArrayList<>();
        var equippedHandler = handler.getEquippedCurios();
        for (int i = 0; i < equippedHandler.getSlots(); i++) {
            ItemStack stack = equippedHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                list.add(stack);
            }
        }
        return list;
    }

    /** 打开 Curios 饰品栏 GUI（服务端调用）。 */
    public static void openCuriosScreen(ServerPlayer player) {
        if (isCuriosLoaded() && player != null) {
            NetworkHooks.openScreen(player, new CuriosContainerProvider());
        }
    }

    private TrinketHelper() {
    }
}
