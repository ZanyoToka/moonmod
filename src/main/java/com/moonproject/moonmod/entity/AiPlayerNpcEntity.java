package com.moonproject.moonmod.entity;

import com.moonproject.moonmod.ai.npc.inventory.AiNpcInventory;
import com.moonproject.moonmod.ai.npc.inventory.NpcInventoryMenu;
import com.moonproject.moonmod.ai.npc.task.NpcTaskManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * 假人玩家实体 (AI Player NPC / Fake Player)
 * 支持：
 * 1. 12 种 3D 骨骼动作、头顶 3D 浮空对话气泡、对话中驻足目视系统
 * 2. 状态机 + 模块化 Task 系统 (待命、护卫巡逻、农场采掘、紧随伴随)
 * 3. 实体真实独立容器 (24格随身行囊：4盔甲、2手持、18背包) 与 NBT 持久化
 * 4. 潜行右键打开管家管理界面 (Shift+Right Click Maid GUI)
 */
public class AiPlayerNpcEntity extends PathfinderMob {

    // 12 种骨骼动作状态枚举
    public static final byte ANIM_IDLE = 0;       // 待机呼吸
    public static final byte ANIM_WAVE = 1;       // 热情招手
    public static final byte ANIM_TALK = 2;       // 说话点头
    public static final byte ANIM_THINK = 3;      // 托腮沉思
    public static final byte ANIM_HAPPY = 4;      // 双手高举欢呼
    public static final byte ANIM_BOW = 5;        // 礼貌鞠躬
    public static final byte ANIM_ANGRY = 6;      // 叉腰气恼跺脚
    public static final byte ANIM_SAD = 7;        // 垂头沮丧
    public static final byte ANIM_CLAP = 8;       // 鼓掌赞赏
    public static final byte ANIM_SURPRISE = 9;   // 惊讶捂嘴后仰
    public static final byte ANIM_PROUD = 10;     // 叉腰昂首挺胸(傲娇)
    public static final byte ANIM_DANCE = 11;     // 快乐摇摆舞

    // 实体数据同步访问器
    private static final EntityDataAccessor<Byte> DATA_ANIM_STATE =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_ANIM_TICKS =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MOOD =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AFFECTION =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_EMOTION =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.STRING);

    // 对话与头顶 3D 气泡同步字段
    private static final EntityDataAccessor<Boolean> DATA_CONVERSING =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_BUBBLE_TEXT =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_BUBBLE_TICKS =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.INT);

    // 行为模式同步字段 (例如 idle, guard, harvest, follow)
    private static final EntityDataAccessor<String> DATA_TASK_MODE =
            SynchedEntityData.defineId(AiPlayerNpcEntity.class, EntityDataSerializers.STRING);

    // 当前正在与之对话的玩家 UUID 及倒计时
    private UUID conversingPlayerUUID = null;
    private int conversingTimer = 0;

    // 专属独立随身行囊容器 (Maid-style Inventory, 24格)
    private final AiNpcInventory npcInventory = new AiNpcInventory(this);
    // 模块化任务调度管理器 (Task State Machine)
    private final NpcTaskManager taskManager = new NpcTaskManager(this);

    public AiPlayerNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("DeepSeek大肥鱼"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();

        // 打上 AI NPC 识别标签
        this.addTag("ai_npc");
        this.addTag("deepseek");
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ANIM_STATE, ANIM_IDLE);
        this.entityData.define(DATA_ANIM_TICKS, 0);
        this.entityData.define(DATA_MOOD, 65);
        this.entityData.define(DATA_AFFECTION, 20);
        this.entityData.define(DATA_EMOTION, "happy");
        this.entityData.define(DATA_CONVERSING, false);
        this.entityData.define(DATA_BUBBLE_TEXT, "");
        this.entityData.define(DATA_BUBBLE_TICKS, 0);
        this.entityData.define(DATA_TASK_MODE, "idle");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // 对话专注目标：驻足停步并专注目视玩家（优先级最高）
        this.goalSelector.addGoal(1, new ConversingLookGoal(this));
        // 常规待机观察玩家
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        // 自由漫步（对话交互中自动静止，绝不到处乱跑）
        this.goalSelector.addGoal(3, new SafeStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    public void tick() {
        super.tick();

        // 1. 骨骼动画倒计时
        int animTicks = this.entityData.get(DATA_ANIM_TICKS);
        if (animTicks > 0) {
            this.entityData.set(DATA_ANIM_TICKS, animTicks - 1);
            if (animTicks - 1 == 0) {
                this.entityData.set(DATA_ANIM_STATE, ANIM_IDLE);
            }
        }

        // 2. 头顶 3D 气泡倒计时
        int bubbleTicks = this.entityData.get(DATA_BUBBLE_TICKS);
        if (bubbleTicks > 0) {
            this.entityData.set(DATA_BUBBLE_TICKS, bubbleTicks - 1);
            if (bubbleTicks - 1 == 0) {
                this.entityData.set(DATA_BUBBLE_TEXT, "");
            }
        }

        // 3. 对话态驻足与玩家专注锁定
        if (!this.level().isClientSide() && this.conversingTimer > 0) {
            this.conversingTimer--;
            Player targetPlayer = getConversingPlayer();
            if (targetPlayer == null || !targetPlayer.isAlive() || this.distanceToSqr(targetPlayer) > 64.0D) {
                stopConversing();
            } else {
                // 停止寻路并面朝玩家
                this.getNavigation().stop();
                this.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);
            }
        }

        // 4. 客户端微观粒子与情绪氛围（性能优化：仅在 16 格内存在活体玩家时才计算粒子）
        if (this.level().isClientSide() && this.level().hasNearbyAlivePlayer(this.getX(), this.getY(), this.getZ(), 16.0D)) {
            if (this.tickCount % 35 == 0 && this.random.nextFloat() < 0.35F) {
                String emotion = getEmotion();
                double px = this.getX() + (this.random.nextDouble() - 0.5) * 0.5;
                double py = this.getY() + 1.8 + this.random.nextDouble() * 0.3;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5) * 0.5;

                switch (emotion.toLowerCase()) {
                    case "happy", "touched" ->
                            this.level().addParticle(ParticleTypes.HEART, px, py, pz, 0, 0.05, 0);
                    case "excited", "dance" ->
                            this.level().addParticle(ParticleTypes.NOTE, px, py, pz, this.random.nextDouble(), 0, 0);
                    case "angry" ->
                            this.level().addParticle(ParticleTypes.ANGRY_VILLAGER, px, py, pz, 0, 0, 0);
                    case "sad" ->
                            this.level().addParticle(ParticleTypes.SPLASH, px, py - 0.5, pz, 0, -0.1, 0);
                    case "clap", "proud" ->
                            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, 0.02, 0);
                }
            }
        }

        // 5. 执行模块化任务状态机调度 (服务端驱动)
        if (!this.level().isClientSide()) {
            this.taskManager.tick();
        }
    }

    /**
     * 打开女仆式管理行囊菜单 (Maid-style GUI)
     */
    public void openInventory(ServerPlayer player) {
        if (this.level().isClientSide() || player == null) return;
        this.npcInventory.syncFromEntity();
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal(AiPlayerNpcEntity.this.getScoreboardName() + " 的行囊与管理");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                return new NpcInventoryMenu(containerId, playerInventory, AiPlayerNpcEntity.this);
            }
        }, buf -> buf.writeVarInt(this.getId()));
    }

    // =========================================================================
    // NBT 完整数据持久化与读取
    // =========================================================================
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("AnimState", getAnimationState());
        tag.putInt("Mood", getMood());
        tag.putInt("Affection", getAffection());
        tag.putString("Emotion", getEmotion());
        tag.putString("TaskMode", this.taskManager.getCurrentTask().getTaskId());
        tag.put("NpcInventory", this.npcInventory.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AnimState")) {
            this.entityData.set(DATA_ANIM_STATE, tag.getByte("AnimState"));
        }
        if (tag.contains("Mood")) {
            setMood(tag.getInt("Mood"));
        }
        if (tag.contains("Affection")) {
            setAffection(tag.getInt("Affection"));
        }
        if (tag.contains("Emotion")) {
            setEmotion(tag.getString("Emotion"));
        }
        if (tag.contains("TaskMode")) {
            String mode = tag.getString("TaskMode");
            this.taskManager.switchTask(mode);
            setTaskMode(mode);
        }
        if (tag.contains("NpcInventory")) {
            this.npcInventory.fromTag(tag.getList("NpcInventory", 10));
        }
    }

    /**
     * 进入对话专注态：立即停止一切移动并面朝玩家
     */
    public void startConversing(Player player, int durationTicks) {
        this.conversingPlayerUUID = (player != null) ? player.getUUID() : null;
        this.conversingTimer = durationTicks;
        this.entityData.set(DATA_CONVERSING, true);
        this.getNavigation().stop();
        if (player != null) {
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
    }

    public void stopConversing() {
        this.conversingPlayerUUID = null;
        this.conversingTimer = 0;
        this.entityData.set(DATA_CONVERSING, false);
    }

    public boolean isConversing() {
        return this.entityData.get(DATA_CONVERSING);
    }

    public Player getConversingPlayer() {
        if (this.conversingPlayerUUID == null) return null;
        return this.level().getPlayerByUUID(this.conversingPlayerUUID);
    }

    /**
     * 在头顶展示 3D 浮空漫画对话气泡
     */
    public void showSpeechBubble(String text, int ticks) {
        if (text != null && !text.isBlank()) {
            this.entityData.set(DATA_BUBBLE_TEXT, text);
            this.entityData.set(DATA_BUBBLE_TICKS, ticks);
        }
    }

    public String getBubbleText() {
        return this.entityData.get(DATA_BUBBLE_TEXT);
    }

    public int getBubbleTicks() {
        return this.entityData.get(DATA_BUBBLE_TICKS);
    }

    public void triggerAnimation(byte animState, int durationTicks) {
        this.entityData.set(DATA_ANIM_STATE, animState);
        this.entityData.set(DATA_ANIM_TICKS, durationTicks);
    }

    public byte getAnimationState() {
        return this.entityData.get(DATA_ANIM_STATE);
    }

    public int getMood() {
        return this.entityData.get(DATA_MOOD);
    }

    public void setMood(int mood) {
        this.entityData.set(DATA_MOOD, Math.max(0, Math.min(100, mood)));
    }

    public int getAffection() {
        return this.entityData.get(DATA_AFFECTION);
    }

    public void setAffection(int affection) {
        this.entityData.set(DATA_AFFECTION, Math.max(0, Math.min(100, affection)));
    }

    public String getEmotion() {
        return this.entityData.get(DATA_EMOTION);
    }

    public void setEmotion(String emotion) {
        this.entityData.set(DATA_EMOTION, emotion != null ? emotion : "happy");
    }

    public String getTaskMode() {
        return this.entityData.get(DATA_TASK_MODE);
    }

    public void setTaskMode(String mode) {
        this.entityData.set(DATA_TASK_MODE, mode != null ? mode : "idle");
    }

    public AiNpcInventory getNpcInventory() {
        return this.npcInventory;
    }

    public NpcTaskManager getTaskManager() {
        return this.taskManager;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    // =========================================================================
    // 专属行为 Goal：对话中保持驻足与注视
    // =========================================================================
    private static class ConversingLookGoal extends Goal {
        private final AiPlayerNpcEntity npc;

        ConversingLookGoal(AiPlayerNpcEntity npc) {
            this.npc = npc;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.npc.isConversing();
        }

        @Override
        public void start() {
            this.npc.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.npc.getNavigation().stop();
            Player player = this.npc.getConversingPlayer();
            if (player != null && player.isAlive()) {
                this.npc.getLookControl().setLookAt(player, 30.0F, 30.0F);
            }
        }
    }

    private static class SafeStrollGoal extends WaterAvoidingRandomStrollGoal {
        private final AiPlayerNpcEntity npc;

        SafeStrollGoal(AiPlayerNpcEntity npc, double speed) {
            super(npc, speed);
            this.npc = npc;
        }

        @Override
        public boolean canUse() {
            return !this.npc.isConversing() && super.canUse();
        }
    }
}
