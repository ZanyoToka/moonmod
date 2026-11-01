package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.config.MoonProjectConfig;
import com.moonproject.moonmod.item.AlayaVijnanaItem;
import com.moonproject.moonmod.item.ModItems;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 阿鼻叫唤（无我良秀被动）：关卡开始时获得「天杀星刀-阿赖耶识」，
 * 并令自身理智值固定为 -44；受到包括沉沦在内的理智伤害时，改为受到 3 倍体力伤害。
 * <p>本模组以「手持阿赖耶识」作为「持有天杀星刀-阿赖耶识」的判定；
 * 同时启用固定恐慌（恐慌类型不被改变）。</p>
 *
 * <p>「出鞘烧血」：只有<b>拔刀态</b>才进入烧血结算——每
 * {@code bloodDrainIntervalTicks}（默认 20 tick = 1 秒）扣掉
 * {@code maxHealth / bloodDrainDivisor}（默认 30，即 30 秒扣完整条血）。
 * 用 {@code setHealth} 直接设值，不走 {@code hurt}，因此没有受伤无敌帧与镜头抖动。</p>
 *
 * <p>数值全部来自 {@code MoonProjectConfig}；{@code bloodDrainDivisor = 0} 关闭烧血。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class AbyssCallEvents {
    /** 原著固定值。 */
    public static final int FIXED_SANITY = -44;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        ItemStack held = player.getMainHandItem();
        boolean holding = held.is(ModItems.ALAYA_VIJNANA.get());
        // 只有「拔刀态」才进入出鞘烧血
        boolean drawn = holding && AlayaVijnanaItem.isDrawnBlade(held);

        Sanity.setAbyssCall(player, holding);
        Sanity.setFixedPanic(player, holding);
        if (holding) {
            // 理智值固定为 -44（不受其他理智变化影响）
            Sanity.set(player, FIXED_SANITY);
        }
        if (drawn) {
            drainBlood(player);
        }
    }

    /**
     * 出鞘烧血：按配置间隔直接扣血。
     *
     * <p>白盒三问：①数据只改 {@code player.setHealth}，不进 NBT；
     * ②纯服务端结算，客户端由原版血量同步包负责显示；
     * ③{@code divisor <= 0} 视为关闭，血量已为 0 时不再结算。</p>
     */
    private static void drainBlood(Player player) {
        double divisor = MoonProjectConfig.bloodDrainDivisor();
        if (divisor <= 0.0D) {
            return;
        }
        int interval = Math.max(1, MoonProjectConfig.bloodDrainIntervalTicks());
        if (player.tickCount % interval != 0) {
            return;
        }
        float health = player.getHealth();
        if (health <= 0.0F) {
            return;
        }
        float next = health - (float) (player.getMaxHealth() / divisor);
        player.setHealth(Math.max(0.0F, next));
    }

    private AbyssCallEvents() {
    }
}
