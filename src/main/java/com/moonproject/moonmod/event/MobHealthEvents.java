package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 全体怪物（含中立生物）最大生命值 x2.5。
 * <p>在实体加入世界时把 MAX_HEALTH 抬到 2.5 倍，并用 NBT 标记避免重复叠加。</p>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class MobHealthEvents {
    public static final String TAG_BOOSTED = "MoonHpBoosted";
    /** 生命倍率。 */
    public static final double MULTIPLIER = 2.5D;
    /** 末影龙固定血量。 */
    public static final double ENDER_DRAGON_HEALTH = 1500.0D;
    /** BOSS 额外倍率。 */
    public static final double BOSS_MULTIPLIER = 1.5D;

    /** 是否为 BOSS（末影龙、凋灵、监守者、远古守卫者）。 */
    public static boolean isBoss(LivingEntity entity) {
        return entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || entity instanceof net.minecraft.world.entity.monster.warden.Warden
                || entity instanceof net.minecraft.world.entity.monster.ElderGuardian;
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (living instanceof Player) {
            return;
        }
        AttributeInstance health = living.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) {
            return;
        }
        CompoundTag data = living.getPersistentData();
        if (data.getBoolean(TAG_BOOSTED)) {
            return;
        }
        data.putBoolean(TAG_BOOSTED, true);
        // 末影龙单独设为 1500，其余生物统一 x2.5
        if (living instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
            health.setBaseValue(ENDER_DRAGON_HEALTH * BOSS_MULTIPLIER);
        } else if (isBoss(living)) {
            // BOSS 额外 x1.5
            health.setBaseValue(health.getBaseValue() * MULTIPLIER * BOSS_MULTIPLIER);
        } else {
            health.setBaseValue(health.getBaseValue() * MULTIPLIER);
        }
        living.setHealth((float) health.getValue());
    }

    private MobHealthEvents() {
    }
}