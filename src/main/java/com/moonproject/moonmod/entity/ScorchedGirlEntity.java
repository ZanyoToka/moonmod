package com.moonproject.moonmod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 焦化少女（TETH）：靠近玩家后点燃自己并自爆。
 *
 * <p>机制：玩家进入 {@link #TRIGGER_RANGE} 格 → 开始引线 {@link #FUSE_TICKS}（3 秒，身体冒火星）
 * → 引线结束自爆（半径 {@link #EXPLODE_RADIUS}，不破坏方块），自身死亡。</p>
 */
public class ScorchedGirlEntity extends Monster {

    /** 触发引线的距离（格）。 */
    public static final double TRIGGER_RANGE = 3.0D;
    /** 引线时长（tick）：3 秒。 */
    public static final int FUSE_TICKS = 20;
    /** 爆炸半径。 */
    public static final float EXPLODE_RADIUS = 200.0F;
    /** 出生后的保护期（tick），期间不会开始引线。 */
    public static final int SPAWN_GRACE_TICKS = 200;

    private int fuse = -1;

    /** 锁定的玩家（仇恨目标），出生时随机选定后不再改变。 */
    private java.util.UUID lockedTarget = null;

    public ScorchedGirlEntity(EntityType<? extends ScorchedGirlEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        // 仇恨机制：出生时随机锁定一名玩家，之后永不改变（见 tick 里的 lockTarget）
    }

    /**
     * 随机锁定一名玩家，并在之后一直保持。
     *
     * <p>规则：出生时从当前世界的玩家中随机选一位（不看距离），把 UUID 记下来；
     * 之后每 tick 把 {@code setTarget} 强制指向他，其它任何仇恨来源都不会改变它。</p>
     */
    private void lockTarget() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        if (lockedTarget == null) {
            java.util.List<net.minecraft.server.level.ServerPlayer> players = server.getPlayers(p -> !p.isSpectator());
            if (players.isEmpty()) {
                return;
            }
            Player chosen = players.get(this.random.nextInt(players.size()));
            lockedTarget = chosen.getUUID();
        }
        Player target = server.getPlayerByUUID(lockedTarget);
        if (target == null) {
            // 目标离线：不做处理，继续等待
            return;
        }
        if (!target.isAlive() || target.isSpectator()) {
            // 锁定的玩家已死亡 -> 立即死亡且不掉落任何东西
            this.setNoAi(true);
            this.discard();
            return;
        }
        this.setTarget(target);
    }

    /** 引线是否在进行。 */
    public boolean isFusing() {
        return fuse >= 0;
    }

    /** 引线剩余比例（1.0 → 0.0），供渲染层做闪烁。 */
    public float fuseProgress() {
        return fuse < 0 ? 0.0F : 1.0F - (float) fuse / FUSE_TICKS;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        // 仇恨锁定：出生时随机选一名玩家，之后每 tick 强制维持为目标
        lockTarget();

        // 引线推进
        if (fuse >= 0) {
            fuse--;
            if (fuse % 10 == 0) {
                this.playSound(SoundEvents.FIRE_AMBIENT, 1.0F, 1.0F);
            }
            if (fuse <= 0) {
                explode();
            }
            return;
        }

        // 出生保护：刚召唤出来的 3 秒内不触发
        if (this.tickCount < SPAWN_GRACE_TICKS) {
            return;
        }

        // 检测玩家是否进入触发范围
        Player nearest = this.level().getNearestPlayer(this, TRIGGER_RANGE);
        if (nearest == null) {
            return;
        }
        // 创造/旁观模式玩家：永远不触发自爆（也顺便让锁定的目标不参与）
        if (nearest.isCreative() || nearest.isSpectator()) {
            return;
        }
        // 锁定的目标如果是创造/旁观，同样不触发
        if (this.getTarget() instanceof Player locked
                && (locked.isCreative() || locked.isSpectator())) {
            return;
        }
        fuse = FUSE_TICKS;
        this.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.2F);
    }

    /** 自爆：不破坏方块，伤害范围内所有生物。 */
    private void explode() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        server.explode(this, this.getX(), this.getY() + 0.5D, this.getZ(),
                EXPLODE_RADIUS, Level.ExplosionInteraction.MOB);
        // 额外补一次火焰伤害，保证数值可控
        AABB box = this.getBoundingBox().inflate(EXPLODE_RADIUS);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, box)) {
            if (victim != this && !(victim instanceof ScorchedGirlEntity)) {
                victim.hurt(server.damageSources().explosion(this, this), 100.0F);
                victim.setSecondsOnFire(3);
            }
        }
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Fuse", fuse);
        if (lockedTarget != null) {
            tag.putUUID("LockedTarget", lockedTarget);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuse = tag.getInt("Fuse");
        if (tag.hasUUID("LockedTarget")) {
            lockedTarget = tag.getUUID("LockedTarget");
        }
    }
}