package com.moonproject.moonmod.entity;

import com.moonproject.moonmod.config.MoonProjectConfig;
import com.moonproject.moonmod.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * 刀光残留：斩击在空气里留下的那道弧光，原地停留一段时间后自行消散。
 *
 * <p><b>白盒：为什么用实体而不是粒子？</b></p>
 * <ul>
 *   <li>粒子的数量有上限（客户端约 1.6 万颗），而且<b>离玩家超过 32 格会被剔除</b>。要「在原地留 30 秒」，
 *       靠粒子既撑不住数量，也留不住远处。</li>
 *   <li>实体是服务端权威的：生成时同步一次，到期由服务端 {@code discard()}（客户端自动收到移除包），
 *       所以任何人从任何角度看，刀光都在同一个位置、同一时刻消失。</li>
 *   <li>实体不参与碰撞、不可被选中、不写入存档（{@code noSave()}），区块卸载即消失——纯装饰，
 *       不会在世界里留下垃圾数据。</li>
 * </ul>
 *
 * <p>数据同步：{@code life/scale/roll/variant} 四个参数放进 {@link SynchedEntityData}，
 * 服务端设置后由网络同步；{@code tickCount} 两端都从 0 开始自增，所以客户端不需要额外的「已存活多久」同步。</p>
 */
public class SlashScarEntity extends Entity {
    /** 剩余寿命（tick）。 */
    private static final EntityDataAccessor<Integer> DATA_LIFE =
            SynchedEntityData.defineId(SlashScarEntity.class, EntityDataSerializers.INT);
    /** 横向宽度（格）。 */
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(SlashScarEntity.class, EntityDataSerializers.FLOAT);
    /** 弧光在屏幕平面内的旋转角（度）。 */
    private static final EntityDataAccessor<Float> DATA_ROLL =
            SynchedEntityData.defineId(SlashScarEntity.class, EntityDataSerializers.FLOAT);
    /** 外观变体：0 = 普通斩击（橙白），1 = 概念抹除（冷白）。 */
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(SlashScarEntity.class, EntityDataSerializers.INT);

    public SlashScarEntity(EntityType<? extends SlashScarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /**
     * 服务端生成一条刀光。
     *
     * @param level    所在世界
     * @param x/y/z    世界坐标（斩击落点）
     * @param roll     屏幕平面内的旋转角
     * @param scale    横向宽度
     * @param variant  0 普通 / 1 概念抹除
     */
    public static SlashScarEntity spawn(Level level, double x, double y, double z,
                                        float roll, float scale, int variant) {
        SlashScarEntity scar = new SlashScarEntity(ModEntities.SLASH_SCAR.get(), level);
        scar.setPos(x, y, z);
        scar.setLife(MoonProjectConfig.slashScarLifetimeTicks());
        scar.setScale(scale);
        scar.setRoll(roll);
        scar.setVariant(variant);
        level.addFreshEntity(scar);
        return scar;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_LIFE, 600);
        this.entityData.define(DATA_SCALE, 3.2F);
        this.entityData.define(DATA_ROLL, 0.0F);
        this.entityData.define(DATA_VARIANT, 0);
    }

    @Override
    public void tick() {
        super.tick();
        // 只有服务端有权决定「到点了」；客户端照常插值，不做任何判定
        if (!this.level().isClientSide && this.tickCount >= this.getLife()) {
            this.discard();
        }
    }

    // ── 纯粹的表现物：不参与任何玩法判定 ──

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        return distanceSqr < 128.0D * 128.0D;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setLife(tag.getInt("Life"));
        this.setScale(tag.getFloat("Scale"));
        this.setRoll(tag.getFloat("Roll"));
        this.setVariant(tag.getInt("Variant"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", this.getLife());
        tag.putFloat("Scale", this.getScale());
        tag.putFloat("Roll", this.getRoll());
        tag.putInt("Variant", this.getVariant());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    // ── 同步字段读写 ──

    public int getLife() {
        return this.entityData.get(DATA_LIFE);
    }

    public void setLife(int life) {
        this.entityData.set(DATA_LIFE, Math.max(1, life));
    }

    public float getScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public void setScale(float scale) {
        this.entityData.set(DATA_SCALE, scale);
    }

    public float getRoll() {
        return this.entityData.get(DATA_ROLL);
    }

    public void setRoll(float roll) {
        this.entityData.set(DATA_ROLL, roll);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }
}
