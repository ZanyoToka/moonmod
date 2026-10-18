package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.item.AlayaVijnanaItem;
import com.moonproject.moonmod.item.ModItems;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 阿鼻叫唤（无我良秀被动）：拔刀态手持「天杀星刀-阿赖耶识」时理智值固定为 -44，
 * 受到包括沉沦在内的理智伤害时改为受到 3 倍体力伤害，并启用固定恐慌。
 *
 * <p>同时伴随「出鞘烧血」：拔刀状态下每 {@link #DRAIN_INTERVAL} tick 直接扣除
 * 最大生命值的 {@code 1/30}（用 {@code setHealth} 直接设值，不触发受伤的抖动效果）。
 * 30 秒正好扣完整条血。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class AbyssCallEvents {
    /** 原著固定值。 */
    public static final int FIXED_SANITY = -44;
    /** 烧血间隔（tick）：每秒一次。 */
    public static final int DRAIN_INTERVAL = 20;
    /** 每次扣除最大生命值的比例倒数：1/30 → 30 次（30 秒）扣完。 */
    public static final float DRAIN_DIVISOR = 30.0F;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        ItemStack held = player.getMainHandItem();
        boolean holding = held.is(ModItems.ALAYA_VIJNANA.get());
        // 只有「拔刀态」才进入阿鼻叫唤与烧血
        boolean drawn = holding && AlayaVijnanaItem.isDrawnBlade(held);

        Sanity.setAbyssCall(player, drawn);
        Sanity.setFixedPanic(player, drawn);
        if (drawn) {
            Sanity.set(player, FIXED_SANITY);

            // 出鞘烧血：每秒扣 maxHealth/30，直接 setHealth，无受伤抖动
            if (player.tickCount % DRAIN_INTERVAL == 0 && player.getHealth() > 0.0F) {
                float next = player.getHealth() - player.getMaxHealth() / DRAIN_DIVISOR;
                player.setHealth(Math.max(0.0F, next));
            }
        }
    }

    private AbyssCallEvents() {
    }
}