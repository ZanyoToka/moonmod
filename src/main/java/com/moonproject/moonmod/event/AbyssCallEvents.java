package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.item.ModItems;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 阿鼻叫唤（无我良秀被动）：关卡开始时获得「天杀星刀-阿赖耶识」，
 * 并令自身理智值固定为 -44；受到包括沉沦在内的理智伤害时，改为受到 3 倍体力伤害。
 * <p>本模组以「手持阿赖耶识」作为「持有天杀星刀-阿赖耶识」的判定；
 * 同时启用固定恐慌（恐慌类型不被改变）。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class AbyssCallEvents {
    /** 原著固定值。 */
    public static final int FIXED_SANITY = -44;
    /** 出鞘状态把满血扣完所需的 tick 数（30 秒）。 */
    /** 掉血间隔（tick）：每 20 tick = 1 秒掉一次。 */
    public static final int DRAIN_INTERVAL = 20;
    /** 总共掉多少次（30 秒 / 1 秒）。 */
    public static final float DRAIN_STEPS = 30.0F;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        // 只有「出鞘（拔刀）状态」才触发阿鼻叫唤；未出鞘时不锁理智、也不豁免虚弱
        net.minecraft.world.item.ItemStack held = player.getMainHandItem();
        boolean holding = held.is(ModItems.ALAYA_VIJNANA.get())
                && com.moonproject.moonmod.item.AlayaVijnanaItem.isDrawnBlade(held);

        Sanity.setAbyssCall(player, holding);
        Sanity.setFixedPanic(player, holding);
        if (holding) {
            // 理智值固定为 -44（不受其他理智变化影响）
            Sanity.set(player, FIXED_SANITY);
            // 出鞘持续流失生命：30 秒扣完满血。
            // 每 20 tick 掉一次（而不是每 tick），避免客户端血量条高频刷新造成"屏幕抖动"的感觉。
            if (player.tickCount % DRAIN_INTERVAL == 0) {
                float max = player.getMaxHealth();
                float next = player.getHealth() - max / DRAIN_STEPS;
                if (next <= 0.0F) {
                    player.setHealth(0.0F);
                    player.hurt(player.damageSources().magic(), 0.5F);
                } else {
                    player.setHealth(next);
                }
            }
        }
    }

    private AbyssCallEvents() {
    }
}