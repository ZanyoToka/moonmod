package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.block.ContainmentUnitBlock;
import com.moonproject.moonmod.block.MeltdownSirenBlock;
import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.network.chat.Component;
import com.moonproject.moonmod.gui.MeltdownSirenMenu;

/**
 * 脑叶警报广播机 (Meltdown Siren BlockEntity):
 * 监测周围 24 格内的异想体收容单元；
 * 当被红石充能，或者检测到 24 格内有收容单元安定度 < 30 时自动拉响最高警戒；
 * 周期性为 32 格内的玩家赋予增益效果（速度/攻击），并使附近的敌对怪物显形发光。
 */
public class MeltdownSirenBlockEntity extends BlockEntity implements MenuProvider {

    public static final int DETECT_RANGE = 24;
    public static final int EFFECT_RANGE = 32;

    private int tickCount = 0;
    private int sirenCooldown = 0;
    private int nearbyMeltdowns = 0;
    private boolean manualOverride = false;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getBlockState().hasProperty(MeltdownSirenBlock.ACTIVE) && getBlockState().getValue(MeltdownSirenBlock.ACTIVE) ? 1 : 0;
                case 1 -> nearbyMeltdowns;
                case 2 -> manualOverride ? 1 : 0;
                case 3 -> EFFECT_RANGE;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 2) {
                manualOverride = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public void toggleManualTrigger(Player player) {
        this.manualOverride = !this.manualOverride;
        if (level != null && getBlockState().hasProperty(MeltdownSirenBlock.ACTIVE)) {
            level.setBlock(worldPosition, getBlockState().setValue(MeltdownSirenBlock.ACTIVE, this.manualOverride), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.meltdown_siren");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MeltdownSirenMenu(containerId, playerInventory, this, this.data);
    }

    public MeltdownSirenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MELTDOWN_SIREN.get(), pos, state);
    }

    public int getNearbyMeltdowns() {
        return nearbyMeltdowns;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MeltdownSirenBlockEntity entity) {
        if (level == null || level.isClientSide) return;

        entity.tickCount++;

        // 每 20 tick (1 秒) 扫描一次周边收容单元和红石状态
        if (entity.tickCount % 20 == 0) {
            boolean redstoneSignal = level.hasNeighborSignal(pos);
            entity.nearbyMeltdowns = entity.scanForCriticalContainmentUnits((ServerLevel) level, pos);
            boolean shouldActivate = redstoneSignal || entity.nearbyMeltdowns > 0;

            if (state.hasProperty(MeltdownSirenBlock.ACTIVE) && state.getValue(MeltdownSirenBlock.ACTIVE) != shouldActivate) {
                level.setBlock(pos, state.setValue(MeltdownSirenBlock.ACTIVE, shouldActivate), 3);
            }
        }

        // 如果处于警报激活状态
        if (state.hasProperty(MeltdownSirenBlock.ACTIVE) && state.getValue(MeltdownSirenBlock.ACTIVE)) {
            entity.sirenCooldown++;

            // 每 40 tick (2 秒) 播放防空大警报声
            if (entity.sirenCooldown >= 40) {
                entity.sirenCooldown = 0;
                level.playSound(null, pos, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 2.5F, 0.65F);
                level.playSound(null, pos, SoundEvents.RAID_HORN.value(), SoundSource.BLOCKS, 2.0F, 0.8F);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                            4, 0.15, 0.1, 0.15, 0.0);
                }
            }

            // 每 20 tick 为 32 格内的玩家与敌对怪物施加紧急状态
            if (entity.tickCount % 20 == 0) {
                AABB effectBox = new AABB(pos).inflate(EFFECT_RANGE);

                // 1. 警戒区域内的玩家：赋予急迫警戒增益（速度 II、力量 I、抗性 I）
                level.getEntitiesOfClass(Player.class, effectBox).forEach(player -> {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, false, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, false, true));
                });

                // 2. 警戒区域内的敌对生物：强制施加发光效果，暴露目标
                level.getEntitiesOfClass(Monster.class, effectBox).forEach(monster -> {
                    monster.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false, false));
                });
            }
        } else {
            entity.sirenCooldown = 0;
        }
    }

    /**
     * 极速扫描以该警报机为中心 24 格内的收容单元，统计逆卡巴拉崩溃危险数
     */
    private int scanForCriticalContainmentUnits(ServerLevel serverLevel, BlockPos centerPos) {
        int criticalCount = 0;
        ChunkPos centerChunk = new ChunkPos(centerPos);
        int chunkRadius = (DETECT_RANGE >> 4) + 1;

        double maxDistSq = DETECT_RANGE * DETECT_RANGE;

        for (int cx = centerChunk.x - chunkRadius; cx <= centerChunk.x + chunkRadius; cx++) {
            for (int cz = centerChunk.z - chunkRadius; cz <= centerChunk.z + chunkRadius; cz++) {
                if (serverLevel.hasChunk(cx, cz)) {
                    LevelChunk chunk = serverLevel.getChunk(cx, cz);
                    for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                        BlockPos targetPos = entry.getKey();
                        if (targetPos.distSqr(centerPos) <= maxDistSq) {
                            if (entry.getValue() instanceof ContainmentUnitBlockEntity unit) {
                                if (unit.hasCore() && unit.getQlipothCounter() < 30) {
                                    criticalCount++;
                                }
                            }
                        }
                    }
                }
            }
        }

        return criticalCount;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TickCount", tickCount);
        tag.putInt("SirenCooldown", sirenCooldown);
        tag.putInt("NearbyMeltdowns", nearbyMeltdowns);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TickCount")) {
            tickCount = tag.getInt("TickCount");
        }
        if (tag.contains("SirenCooldown")) {
            sirenCooldown = tag.getInt("SirenCooldown");
        }
        if (tag.contains("NearbyMeltdowns")) {
            nearbyMeltdowns = tag.getInt("NearbyMeltdowns");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
