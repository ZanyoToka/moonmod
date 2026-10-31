package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.block.GoldenBoughShrineBlock;
import com.moonproject.moonmod.gui.GoldenBoughShrineMenu;
import com.moonproject.moonmod.init.ModBlockEntities;
import com.moonproject.moonmod.item.ModItems;
import com.moonproject.moonmod.sanity.Sanity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 【黄金枝基座 / 光之种发芽坛】方块实体 (Golden Bough Shrine / Seed of Light Sprout)
 * <p>脑叶公司最高奇点遗产与卡门的光之种共振圣坛。
 * 插入黄金枝后在周围 48 格构建不可侵犯的「光之领域」：
 * 领域内所有人类理智值强制锁定拉满（+45），驱散一切精神崩溃与负面诅咒；领域内异想体与敌对生物被彻底镇静。</p>
 */
public class GoldenBoughShrineBlockEntity extends BlockEntity implements MenuProvider {

    public static final double RADIUS = 48.0D; // 光之领域半径 48 格
    private int chimeCooldown = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            boolean has = !getStackInSlot(0).isEmpty();
            if (level != null) {
                BlockState state = level.getBlockState(worldPosition);
                if (state.hasProperty(GoldenBoughShrineBlock.HAS_BOUGH) && state.getValue(GoldenBoughShrineBlock.HAS_BOUGH) != has) {
                    level.setBlock(worldPosition, state.setValue(GoldenBoughShrineBlock.HAS_BOUGH, has), 3);
                }
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isValidBough(stack);
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> hasBough() ? 1 : 0;
                case 1 -> (int) RADIUS;
                case 2 -> chimeCooldown;
                case 3 -> 100;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 4;
        }
    };

    public GoldenBoughShrineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOLDEN_BOUGH_SHRINE.get(), pos, state);
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

    public boolean hasBough() {
        return !itemHandler.getStackInSlot(0).isEmpty();
    }

    public static boolean isValidBough(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String desc = stack.getItem().getDescriptionId().toLowerCase();
        return desc.contains("bough") || desc.contains("branch")
                || stack.is(ModItems.EGO_CORE.get())
                || stack.is(Items.ENCHANTED_GOLDEN_APPLE)
                || stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.NETHER_STAR);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GoldenBoughShrineBlockEntity entity) {
        if (!entity.hasBough()) return;

        AABB sphere = new AABB(pos).inflate(RADIUS);

        if (level.getGameTime() % 10 == 0) {
            List<Player> players = level.getEntitiesOfClass(Player.class, sphere);
            for (Player player : players) {
                if (Sanity.hasSanity(player)) {
                    Sanity.set(player, 45);
                }
                player.getActiveEffects().removeIf(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL);
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false));
            }
        }

        if (level.getGameTime() % 20 == 0) {
            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, sphere);
            for (Mob mob : mobs) {
                mob.setTarget(null);
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false));
                mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, true, false));
            }
        }

        if (--entity.chimeCooldown <= 0) {
            entity.chimeCooldown = 120;
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.2F, 1.2F);
            level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.4F);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.golden_bough_shrine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GoldenBoughShrineMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
