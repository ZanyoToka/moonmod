package com.moonproject.moonmod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import com.moonproject.moonmod.config.MoonProjectConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 焦化少女（TETH）：靠近玩家后点燃自己并自爆。
 *
 * <p>机制：像苦力怕一样纯自爆——靠近玩家 → 点燃引线 → 引线结束自爆
 * （默认半径 200 格、固定 100 伤害、破坏地形），自身死亡。<b>不做近战</b>：
 * 没有 {@code MeleeAttackGoal}，攻击力属性也置 0。</p>
 *
 * <p>数值全部来自 {@code MoonProjectConfig} 的 {@code scorched_girl} 段：
 * {@code triggerRange} / {@code fuseTicks} / {@code explodeRadius} / {@code explodeDamage} / {@code griefing}。
 * 默认值 = 原作者手调的数值（1 格 / 1 tick / 200 格 / 100 伤害 / 会炸地形）。</p>
 */
public class ScorchedGirlEntity extends Monster {

    /** 出生后的保护期（tick），期间不会开始引线。 */
    public static final int SPAWN_GRACE_TICKS = 20;
    /** 生成时要求离玩家至少这么远（格），避免直接刷在玩家身边。 */
    public static final double MIN_SPAWN_DISTANCE = 24.0D;

    /** 锁定目标时的最大搜索半径（格）：只标记 500 格内的玩家。 */
    public static final double LOCK_RANGE = 500.0D;

    private int fuse = -1;

    /** 锁定的玩家（仇恨目标），出生时随机选定后不再改变。 */
    private java.util.UUID lockedTarget = null;

    public ScorchedGirlEntity(EntityType<? extends ScorchedGirlEntity> type, Level level) {
        super(type, level);
        this.xpReward = 25;   // 与精英怪相同
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                // 纯自爆单位：不挥刀，伤害全部来自爆炸
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, MoonProjectConfig.scorchedGirlMoveSpeed())
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 苦力怕式：没有 MeleeAttackGoal（不会挥刀），只负责靠过去送炸
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.0D, 32.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        // 仇恨机制：出生时随机锁定一名玩家，之后永不改变（见 tick 里的 lockTarget）
    }

    /**
     * 判断某个位置是否可以生成焦化少女：周围 {@link #MIN_SPAWN_DISTANCE} 格内不能有玩家。
     *
     * <p>自然生成 / 采集单元生成都要先过这一步，避免直接刷到玩家身边。</p>
     */
    public static boolean canSpawnAt(Level level, net.minecraft.core.BlockPos pos) {
        double minSq = MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE;
        for (Player p : level.players()) {
            if (p.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < minSq) {
                return false;
            }
        }
        return true;
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
            java.util.List<net.minecraft.server.level.ServerPlayer> players = server.getPlayers(p ->
                !p.isSpectator() && p.distanceToSqr(this) <= LOCK_RANGE * LOCK_RANGE);
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
        return fuse < 0 ? 0.0F : 1.0F - (float) fuse / Math.max(1, MoonProjectConfig.scorchedGirlFuseTicks());
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
        Player nearest = this.level().getNearestPlayer(this, MoonProjectConfig.scorchedGirlTriggerRange());
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
        fuse = MoonProjectConfig.scorchedGirlFuseTicks();
        this.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.2F);
    }

    /** 自爆：伤害范围内所有生物（是否破坏方块由 {@code scorched_girl.griefing} 决定）。 */
    private void explode() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        // 最终检查：只要附近有创造/旁观玩家，就绝不爆炸
        for (Player p : server.getPlayers(p -> p.distanceToSqr(this) < 64.0D)) {
            if (p.isCreative() || p.isSpectator()) {
                this.fuse = -1;
                return;
            }
        }
        float radius = (float) MoonProjectConfig.scorchedGirlExplodeRadius();
        float bonusDamage = (float) MoonProjectConfig.scorchedGirlExplodeDamage();
        Level.ExplosionInteraction interaction = MoonProjectConfig.scorchedGirlGriefing()
                ? Level.ExplosionInteraction.MOB
                : Level.ExplosionInteraction.NONE;
        server.explode(this, this.getX(), this.getY() + 0.5D, this.getZ(), radius, interaction);
        // 额外补一次固定伤害，保证数值可控（不受爆炸衰减影响）
        AABB box = this.getBoundingBox().inflate(radius);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, box)) {
            if (victim != this && !(victim instanceof ScorchedGirlEntity)) {
                if (bonusDamage > 0.0F) {
                    victim.hurt(server.damageSources().explosion(this, this), bonusDamage);
                }
                victim.setSecondsOnFire(3);
            }
        }
        // 自爆后可能留下「终末火柴」（焦化少女的异想体核心，收容单元的原料）
        float dropChance = (float) MoonProjectConfig.scorchedGirlCoreDropChance();
        if (dropChance > 0.0F && server.random.nextFloat() < dropChance) {
            this.spawnAtLocation(new net.minecraft.world.item.ItemStack(
                    com.moonproject.moonmod.item.ModItems.FINAL_MATCH.get()));
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
        // 校验：fuse 必须是 -1（未点燃）或 1..当前引线时长（点燃中），其它值一律重置
        if (fuse < -1 || fuse > MoonProjectConfig.scorchedGirlFuseTicks()) {
            fuse = -1;
        }
        if (fuse == 0) {
            fuse = -1;
        }
        if (tag.hasUUID("LockedTarget")) {
            lockedTarget = tag.getUUID("LockedTarget");
        }
    }
}