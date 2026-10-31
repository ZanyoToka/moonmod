package com.moonproject.moonmod.entity;

import com.moonproject.moonmod.block.MephistophelesSeatBlock;
import com.moonproject.moonmod.effect.ModEffects;
import com.moonproject.moonmod.init.ModEntities;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * 边狱巴士「梅菲斯特客席」骑乘实体（V2 精准坐姿优化版）。
 * <p>精准计算骑乘高度与朝向，杜绝悬空浮空与水平错位。
 * 承载边狱巴士的原著特色机制：理智平复、车厢舒缓与旅途启程加成。</p>
 */
public class SeatEntity extends Entity {
    private BlockPos seatPos;
    private int emptyTicks = 0;
    private int sitTicks = 0;

    public SeatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos pos, double x, double y, double z, float yaw) {
        this(ModEntities.SEAT.get(), level);
        this.seatPos = pos;
        this.setPos(x, y, z);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yRotO = yaw;
    }

    @Override
    protected void defineSynchedData() {
        // 无需额外的同步数据
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        // 1. 检查方块是否仍然存在且有效
        if (this.seatPos != null && !(this.level().getBlockState(this.seatPos).getBlock() instanceof MephistophelesSeatBlock)) {
            this.ejectPassengers();
            this.discard();
            return;
        }

        // 2. 检查是否有乘客
        if (this.getPassengers().isEmpty()) {
            this.emptyTicks++;
            if (this.emptyTicks > 20) {
                this.discard();
            }
            return;
        } else {
            this.emptyTicks = 0;
        }

        // 3. 边狱巴士特色机制：休憩与理智平复
        Entity passenger = this.getFirstPassenger();
        if (passenger instanceof ServerPlayer player) {
            sitTicks++;

            // 每 20 ticks (1 秒) 恢复理智值
            if (sitTicks % 20 == 0) {
                int currentSp = Sanity.get(player);
                if (currentSp < Sanity.MAX) {
                    // 若理智值为负（恐慌濒临阶段），恢复速度翻倍 (+2)
                    int healAmount = currentSp < 0 ? 2 : 1;
                    Sanity.add(player, healAmount);

                    int afterSp = Sanity.get(player);
                    if (afterSp >= Sanity.MAX) {
                        player.displayClientMessage(
                                Component.literal("§a[梅菲斯特号] §f理智值已恢复至充盈状态 (+45 SP)"),
                                true
                        );
                    }
                }

                // 产生轻柔舒缓的心灵治愈微光粒子
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.GLOW,
                            this.getX() + (this.random.nextDouble() - 0.5D) * 0.5D,
                            this.getY() + 0.4D + this.random.nextDouble() * 0.4D,
                            this.getZ() + (this.random.nextDouble() - 0.5D) * 0.5D,
                            1, 0.0D, 0.02D, 0.0D, 0.01D
                    );
                }
            }

            // 每 40 ticks (2 秒) 舒缓负面异常状态与给予轻微生命恢复
            if (sitTicks % 40 == 0) {
                // 缓慢恢复生命
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false, true));

                // 舒缓常见战斗负面效果
                if (player.hasEffect(MobEffects.WEAKNESS)) player.removeEffect(MobEffects.WEAKNESS);
                if (player.hasEffect(MobEffects.CONFUSION)) player.removeEffect(MobEffects.CONFUSION);
                if (player.hasEffect(ModEffects.BLACK_SCAR.get())) player.removeEffect(ModEffects.BLACK_SCAR.get());
                if (player.hasEffect(ModEffects.SINKING.get())) {
                    player.removeEffect(ModEffects.SINKING.get());
                }

                // 播放平缓的钟表引擎律动节拍音
                if (sitTicks % 120 == 0) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.NOTE_BLOCK_CHIME.get(), SoundSource.BLOCKS, 0.5F, 1.2F);
                }
            }
        }
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) {
            return;
        }

        // 核心修复：严谨计算玩家坐姿的垂直偏置！
        // 对于玩家，passenger.getMyRidingOffset() 通常为 -0.35D。
        // 原版中渲染玩家坐姿时，臀部骨骼比脚底高出 0.35D。
        // 加上 passenger.getMyRidingOffset() 后，玩家臀部正好严丝合缝紧贴在 this.getY() 的坐垫面上！
        double ridingOffset = passenger.getMyRidingOffset();
        double yPos = this.getY() + ridingOffset;

        moveFunction.accept(passenger, this.getX(), yPos, this.getZ());

        // 固定乘客身体朝向与座椅对齐，保持坐姿端正，但允许头部自由转动
        if (passenger instanceof LivingEntity living) {
            living.yBodyRot = this.getYRot();
            living.yBodyRotO = this.getYRot();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        // 玩家离座时，给予安全的站立位置（向椅子前方挪开 0.6 格），避免卡在方块碰撞箱内
        if (passenger instanceof ServerPlayer player) {
            Direction facing = Direction.fromYRot(this.getYRot());
            double exitX = this.getX() + facing.getStepX() * 0.65D;
            double exitY = Math.floor(this.getY()) + 0.05D;
            double exitZ = this.getZ() + facing.getStepZ() * 0.65D;
            player.teleportTo(exitX, exitY, exitZ);

            if (this.sitTicks >= 60) {
                // 坐满 3 秒以上离座获得充沛增益
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, false, true));
                player.displayClientMessage(
                        Component.literal("§e[梅菲斯特号] §f“整装待发，下一个站点即将抵达。”"),
                        true
                );
            }
        }
    }

    public BlockPos getSeatPos() {
        return this.seatPos;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("SeatX")) {
            this.seatPos = new BlockPos(
                    compound.getInt("SeatX"),
                    compound.getInt("SeatY"),
                    compound.getInt("SeatZ")
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.seatPos != null) {
            compound.putInt("SeatX", this.seatPos.getX());
            compound.putInt("SeatY", this.seatPos.getY());
            compound.putInt("SeatZ", this.seatPos.getZ());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
