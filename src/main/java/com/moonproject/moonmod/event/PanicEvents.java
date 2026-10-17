package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.sanity.PanicType;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 恐慌 / 理智负面效果：
 * <ul>
 *   <li>理智值<strong>高</strong>（高于 {@link #WEAKNESS_THRESHOLD}）→ <strong>不添加任何 buff</strong></li>
 *   <li>理智值<strong>低</strong> → 施加<strong>虚弱</strong>（理智越低等级越高）</li>
 * </ul>
 * 恐慌类型仍会记录（供「固定恐慌」使用），但不再施加力量/速度之类的增益。
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class PanicEvents {
    public static final String TAG_PANIC = "MoonPanicType";
    /** 低于该理智值开始虚弱。 */
    public static final int WEAKNESS_THRESHOLD = -10;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        if (player.tickCount % 20 != 0) {
            return;
        }
        // 理智被锁定（手持阿赖耶识 / 阿鼻叫唤）时不施加虚弱
        if (Sanity.hasAbyssCall(player)) {
            player.getPersistentData().putString(TAG_PANIC, PanicType.NONE.name());
            return;
        }

        int sanity = Sanity.get(player);
        CompoundTag data = player.getPersistentData();
        String stored = data.getString(TAG_PANIC);
        PanicType current = stored.isEmpty() ? PanicType.NONE : PanicType.valueOf(stored);

        if (sanity > WEAKNESS_THRESHOLD) {
            // 理智还不算低：不加任何 buff
            if (current != PanicType.NONE) {
                data.putString(TAG_PANIC, PanicType.NONE.name());
            }
            return;
        }

        // 理智偏低：记录恐慌类型（固定恐慌时保持不变）
        if (current == PanicType.NONE || !Sanity.hasFixedPanic(player)) {
            if (current == PanicType.NONE) {
                current = PanicType.values()[1 + player.getRandom().nextInt(PanicType.values().length - 1)];
                data.putString(TAG_PANIC, current.name());
            }
        }

        // 施加「虚弱」：理智越低等级越高（-10 -> I，-25 -> II，-45 -> IV）
        int amplifier = Math.max(0, Math.min(3, (-sanity - WEAKNESS_THRESHOLD) / 10));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, amplifier, false, true));
    }

    private PanicEvents() {
    }
}