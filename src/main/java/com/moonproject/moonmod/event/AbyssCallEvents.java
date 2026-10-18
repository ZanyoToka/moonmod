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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        boolean holding = player.getMainHandItem().is(ModItems.ALAYA_VIJNANA.get());

        Sanity.setAbyssCall(player, holding);
        Sanity.setFixedPanic(player, holding);
        if (holding) {
            // 理智值固定为 -44（不受其他理智变化影响）
            Sanity.set(player, FIXED_SANITY);
        }
    }

    private AbyssCallEvents() {
    }
}