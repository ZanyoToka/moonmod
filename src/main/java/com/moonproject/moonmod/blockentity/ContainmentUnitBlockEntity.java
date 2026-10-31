package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.block.ContainmentUnitBlock;
import com.moonproject.moonmod.block.ReinforcedPlatingBlock;
import com.moonproject.moonmod.init.ModBlockEntities;
import com.moonproject.moonmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.moonproject.moonmod.gui.ContainmentUnitMenu;

/**
 * 异想体收容单元 (Containment Unit BlockEntity):
 * 负责保存异想体核心，维持逆卡巴拉计数（Qlipoth Counter，0~100）。
 * 当计数降至 0 时触发收容失效（Meltdown），实体破门而出。
 */
public class ContainmentUnitBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MAX_QLIPOTH = 100;

    /** 逆卡巴拉计数：0 ~ 100，默认 100 */
    private int qlipothCounter = MAX_QLIPOTH;
    private int tickCounter = 0;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> qlipothCounter;
                case 1 -> MAX_QLIPOTH;
                case 2 -> level != null ? ReinforcedPlatingBlock.countAdjacentPlatings(level, worldPosition) : 0;
                case 3 -> qlipothCounter <= 30 ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                setQlipothCounter(value);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    /** 内部物品槽位：1 个槽位用于放置异想体核心 */
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return true; // 允许放入各种异想体核心/碎片
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ContainmentUnitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTAINMENT_UNIT.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public int getQlipothCounter() {
        return qlipothCounter;
    }

    public void setQlipothCounter(int value) {
        int oldVal = this.qlipothCounter;
        this.qlipothCounter = Math.max(0, Math.min(MAX_QLIPOTH, value));
        if (oldVal != this.qlipothCounter) {
            setChanged();
            if (level != null) {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
                updateBlockStateAlert();
            }
        }
    }

    public boolean modifyQlipoth(int delta) {
        int target = this.qlipothCounter + delta;
        setQlipothCounter(target);
        return this.qlipothCounter <= 0;
    }

    public boolean hasCore() {
        return !itemHandler.getStackInSlot(0).isEmpty();
    }

    public ItemStack getCore() {
        return itemHandler.getStackInSlot(0);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.containment_unit");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ContainmentUnitMenu(containerId, playerInventory, this, this.data);
    }

    /**
     * 紧急逆卡巴拉抑制：消耗玩家少量理智或经验，恢复 25 点安定度
     */
    public void suppressMeltdown(Player player) {
        if (this.qlipothCounter < MAX_QLIPOTH) {
            this.setQlipothCounter(this.qlipothCounter + 25);
            if (this.level != null) {
                this.level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.5F);
            }
            player.displayClientMessage(Component.literal("§a[收容单元] 逆卡巴拉抑制程序注入成功，安定度 +25！"), true);
        }
    }

    /**
     * 将内部物品掉落
     */
    public void drops() {
        if (level != null && !level.isClientSide) {
            SimpleContainer container = new SimpleContainer(itemHandler.getSlots());
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                container.setItem(i, itemHandler.getStackInSlot(i));
            }
            Containers.dropContents(level, worldPosition, container);
        }
    }

    /**
     * 根据当前安定度更新方块的警报外观状态
     */
    private void updateBlockStateAlert() {
        if (level == null || level.isClientSide) return;
        BlockState currentState = getBlockState();
        if (currentState.hasProperty(ContainmentUnitBlock.ALERT)) {
            boolean shouldAlert = hasCore() && qlipothCounter < 30;
            if (currentState.getValue(ContainmentUnitBlock.ALERT) != shouldAlert) {
                level.setBlock(worldPosition, currentState.setValue(ContainmentUnitBlock.ALERT, shouldAlert), 3);
            }
        }
    }

    /**
     * 服务端周期执行逻辑
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ContainmentUnitBlockEntity entity) {
        if (level == null || level.isClientSide) return;

        // 如果没有装载异想体核心，收容单元处于闲置维护状态，缓慢恢复安定度
        if (!entity.hasCore()) {
            if (entity.qlipothCounter < MAX_QLIPOTH) {
                entity.tickCounter++;
                if (entity.tickCounter >= 20) {
                    entity.tickCounter = 0;
                    entity.setQlipothCounter(entity.qlipothCounter + 1);
                }
            }
            return;
        }

        // 装载有核心：进行自然衰减检测
        entity.tickCounter++;

        // 检测收容室 6 面安装的「都市重装防爆壁」数量（0~6）
        int platings = ReinforcedPlatingBlock.countAdjacentPlatings(level, pos);
        // 防爆壁越多，逆卡巴拉衰减越慢：基准 80 tick (4秒) + 每块防爆壁延长 40 tick (最多 320 tick / 16秒)
        int decayInterval = 80 + (platings * 40);

        if (entity.tickCounter >= decayInterval) {
            entity.tickCounter = 0;
            entity.modifyQlipoth(-1);
        }

        // 当安定度归零，触发毁灭性收容失效（Breach）
        if (entity.qlipothCounter <= 0) {
            entity.triggerBreach((ServerLevel) level, pos);
        }
    }

    /**
     * 触发收容失效 (Containment Breach):
     * 狂暴异想体破体而出，销毁收容核心，对周围发出最高级别警报并重置计数。
     */
    private void triggerBreach(ServerLevel serverLevel, BlockPos pos) {
        // 1. 播放恐怖警报音效与震荡破坏
        serverLevel.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 2.0F, 0.7F);
        serverLevel.playSound(null, pos, SoundEvents.WARDEN_ROAR, SoundSource.BLOCKS, 2.0F, 0.8F);

        // 2. 产生剧烈的崩溃粒子风暴
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                1, 0, 0, 0, 0.0);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                40, 0.6, 0.8, 0.6, 0.1);

        // 3. 在上方生成狂暴异想体化身（拥有高血量、强力武装的狂暴实体）
        WitherSkeleton anomalyEntity = EntityType.WITHER_SKELETON.create(serverLevel);
        if (anomalyEntity != null) {
            anomalyEntity.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    serverLevel.random.nextFloat() * 360F, 0.0F);
            anomalyEntity.setCustomName(Component.literal("§4[收容失效] 狂暴异想体"));
            anomalyEntity.setCustomNameVisible(true);

            // 增强属性：80 点高额生命值与强化伤害
            if (anomalyEntity.getAttribute(Attributes.MAX_HEALTH) != null) {
                anomalyEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(80.0D);
                anomalyEntity.setHealth(80.0F);
            }
            if (anomalyEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                anomalyEntity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12.0D);
            }
            if (anomalyEntity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                anomalyEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
            }

            // 配备脑叶模组装备
            anomalyEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.REGRET.get()));
            anomalyEntity.setDropChance(EquipmentSlot.MAINHAND, 0.05F);

            serverLevel.addFreshEntity(anomalyEntity);
        }

        // 4. 销毁收容核心
        this.itemHandler.setStackInSlot(0, ItemStack.EMPTY);

        // 5. 重置逆卡巴拉计数与警报状态
        this.qlipothCounter = MAX_QLIPOTH;
        this.tickCounter = 0;
        setChanged();
        updateBlockStateAlert();

        // 6. 全局通知收容失效事件
        serverLevel.players().stream()
                .filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 2500) // 50格内
                .forEach(p -> p.sendSystemMessage(Component.literal(
                        "§4§l[脑叶公司警报] §c收容室 (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() +
                                ") 逆卡巴拉计数归零！异想体已突破收容！")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("QlipothCounter", qlipothCounter);
        tag.putInt("TickCounter", tickCounter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (tag.contains("QlipothCounter")) {
            qlipothCounter = tag.getInt("QlipothCounter");
        }
        if (tag.contains("TickCounter")) {
            tickCounter = tag.getInt("TickCounter");
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
