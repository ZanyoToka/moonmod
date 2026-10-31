package com.moonproject.moonmod.trinket;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.item.AbnormalityTrinketItem;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 饰品效果结算：每 tick 把「当前佩戴的饰品」的属性加成挂到玩家身上。
 *
 * <p>做法是「先移除、再按需添加」，保证摘下饰品后加成立刻消失，不会叠加。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class TrinketEffects {

    /** 加成用的固定 UUID（同一个模组只能有一个，靠 remove 再 add 来刷新）。 */
    private static final Map<String, UUID> MODIFIER_IDS = new HashMap<>();

    /** 每个饰品 coreId 提供的属性加成。 */
    private static void applyBonuses(Player player, ItemStack stack) {
        // 终末火柴之光：最大生命值 +4
        if (stack.getItem() instanceof AbnormalityTrinketItem trinket
                && "scorched_girl".equals(trinket.getCoreId())) {
            addModifier(player, Attributes.MAX_HEALTH, "final_match_light_hp", 4.0D);
        }
    }

    /** 添加一个加法型属性修饰符。 */
    private static void addModifier(Player player, Attribute attribute, String key, double amount) {
        var instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        UUID id = MODIFIER_IDS.computeIfAbsent(key,
                k -> UUID.nameUUIDFromBytes(("moonproject:" + k).getBytes()));
        // 先移除同名（同 UUID）的旧修饰符，避免叠加
        if (instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
        instance.addTransientModifier(new AttributeModifier(id, key, amount,
                AttributeModifier.Operation.ADDITION));
    }

    /** 移除某个 key 对应的修饰符。 */
    private static void removeModifier(Player player, Attribute attribute, String key) {
        var instance = player.getAttribute(attribute);
        UUID id = MODIFIER_IDS.get(key);
        if (instance != null && id != null && instance.getModifier(id) != null) {
            instance.removeModifier(id);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        ItemStack worn = TrinketProvider.get(player).getTrinket(ITrinketData.SLOT_HEAD);

        // 每 tick 先清掉我们加的加成，再按当前饰品重新加
        removeModifier(player, Attributes.MAX_HEALTH, "final_match_light_hp");
        if (!worn.isEmpty()) {
            applyBonuses(player, worn);
        }
    }

    private TrinketEffects() {
    }
}