package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.block.ContainmentUnitBlock;
import com.moonproject.moonmod.block.EnkephalinExtractorBlock;
import com.moonproject.moonmod.init.ModBlockEntities;
import com.moonproject.moonmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import org.joml.Vector3f;

import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import com.moonproject.moonmod.gui.EnkephalinExtractorMenu;

/**
 * 脑啡肽提取终端 (Enkephalin Extractor BlockEntity):
 * 检测水平相邻的异想体收容单元，消耗催化剂（红石、青金石、萤石等），
 * 经过持续运转提炼产出「脑啡肽」（异想体碎片），同时消耗收容单元的安定度。
 */
public class EnkephalinExtractorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int WORK_TIME = 100; // 提取一个脑啡肽所需的 tick 时间（5秒）
    public static final int QLIPOTH_COST = 5; // 每次成功提取扣减的逆卡巴拉计数

    private int progress = 0;
    private boolean isWorking = false;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> WORK_TIME;
                case 2 -> isWorking ? 1 : 0;
                case 3 -> level != null && findAdjacentContainmentUnit() != null ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    /**
     * 槽位 0: 催化剂槽
     * 槽位 1: 脑啡肽产物槽
     */
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return isCatalyst(stack);
            }
            return false; // 产物槽禁止手动或外部输入
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0) {
                // 允许取出催化剂
                return super.extractItem(slot, amount, simulate);
            }
            // 允许取出产出物
            return super.extractItem(slot, amount, simulate);
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public EnkephalinExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENKEPHALIN_EXTRACTOR.get(), pos, state);
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

    public int getProgress() {
        return progress;
    }

    public boolean isWorking() {
        return isWorking;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.enkephalin_extractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EnkephalinExtractorMenu(containerId, playerInventory, this, this.data);
    }

    /**
     * 判断物品是否可用作提取催化剂
     */
    public static boolean isCatalyst(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(Items.REDSTONE)
                || stack.is(Items.LAPIS_LAZULI)
                || stack.is(Items.GLOWSTONE_DUST)
                || stack.is(Items.AMETHYST_SHARD)
                || stack.is(Items.EMERALD)
                || stack.is(Items.BLAZE_POWDER);
    }

    /**
     * 寻找水平相邻（前后左右）的收容单元
     */
    @Nullable
    public ContainmentUnitBlockEntity findAdjacentContainmentUnit() {
        if (level == null) return null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos targetPos = worldPosition.relative(direction);
            BlockEntity be = level.getBlockEntity(targetPos);
            if (be instanceof ContainmentUnitBlockEntity unit) {
                if (unit.hasCore() && unit.getQlipothCounter() > 0) {
                    return unit;
                }
            }
        }
        return null;
    }

    /**
     * 将方块内部物品全部散落
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
     * 服务端工作循环
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, EnkephalinExtractorBlockEntity entity) {
        if (level == null || level.isClientSide) return;

        ContainmentUnitBlockEntity unit = entity.findAdjacentContainmentUnit();
        ItemStack catalyst = entity.itemHandler.getStackInSlot(0);
        ItemStack output = entity.itemHandler.getStackInSlot(1);

        boolean hasCatalyst = isCatalyst(catalyst);
        boolean canOutput = output.isEmpty() || (output.is(ModItems.ABNORMALITY_FRAGMENT.get()) && output.getCount() < output.getMaxStackSize());
        boolean canWork = unit != null && hasCatalyst && canOutput;

        if (canWork) {
            entity.progress++;
            entity.setWorkingState(true);

            // 周期性播放工作机械蜂鸣与科技连接粒子
            if (entity.progress % 20 == 0) {
                level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.4F, 1.8F);
                level.playSound(null, pos, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS, 0.3F, 1.5F);

                if (level instanceof ServerLevel serverLevel) {
                    // 在终端与收容单元之间发射青色连接能量粒子
                    BlockPos unitPos = unit.getBlockPos();
                    double midX = (pos.getX() + unitPos.getX() + 1.0) / 2.0;
                    double midY = (pos.getY() + unitPos.getY() + 1.0) / 2.0;
                    double midZ = (pos.getZ() + unitPos.getZ() + 1.0) / 2.0;

                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            midX, midY + 0.2, midZ, 6, 0.2, 0.2, 0.2, 0.05);
                    serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.0F, 0.9F, 0.85F), 1.0F),
                            pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.02);
                }
            }

            // 提取周期完成
            if (entity.progress >= WORK_TIME) {
                entity.progress = 0;

                // 消耗 1 份催化剂
                entity.itemHandler.extractItem(0, 1, false);

                // 产出 1 个脑啡肽（异想体碎片）
                ItemStack result = new ItemStack(ModItems.ABNORMALITY_FRAGMENT.get());
                if (output.isEmpty()) {
                    entity.itemHandler.setStackInSlot(1, result);
                } else {
                    output.grow(1);
                }

                // 扣减收容单元逆卡巴拉计数
                unit.modifyQlipoth(-QLIPOTH_COST);

                // 产出音效
                level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.6F, 1.2F);
                level.playSound(null, pos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.BLOCKS, 0.5F, 1.4F);
            }
            entity.setChanged();
        } else {
            if (entity.progress != 0 || entity.isWorking) {
                entity.progress = 0;
                entity.setWorkingState(false);
                entity.setChanged();
            }
        }
    }

    private void setWorkingState(boolean working) {
        if (this.isWorking != working) {
            this.isWorking = working;
            if (level != null && !level.isClientSide) {
                BlockState state = getBlockState();
                if (state.hasProperty(EnkephalinExtractorBlock.WORKING) && state.getValue(EnkephalinExtractorBlock.WORKING) != working) {
                    level.setBlock(worldPosition, state.setValue(EnkephalinExtractorBlock.WORKING, working), 3);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("Progress", progress);
        tag.putBoolean("IsWorking", isWorking);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (tag.contains("Progress")) {
            progress = tag.getInt("Progress");
        }
        if (tag.contains("IsWorking")) {
            isWorking = tag.getBoolean("IsWorking");
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
