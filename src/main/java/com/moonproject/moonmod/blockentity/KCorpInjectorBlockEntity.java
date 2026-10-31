package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.gui.KCorpInjectorMenu;
import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 * 【K公司再生安瓿灌装机】方块实体 (K-Corp Regenerative Ampule Injector)
 * <p>都市南部之翼——K公司的生命奇点工业设施。
 * 储蓄着淡青色的微纳米眼泪原液，靠近时形成雾化治疗场；支持使用空玻璃瓶分装高浓缩「K公司再生安瓿」。</p>
 */
public class KCorpInjectorBlockEntity extends BlockEntity implements MenuProvider {

    private int sprayCooldown = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) return stack.is(Items.GLASS_BOTTLE);
            return false;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> 1000;
                case 1 -> 1000;
                case 2 -> sprayCooldown > 0 ? 1 : 0;
                case 3 -> 0;
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

    public KCorpInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KCORP_INJECTOR.get(), pos, state);
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

    public void fillAmpoule(Player player) {
        ItemStack bottle = itemHandler.getStackInSlot(0);
        ItemStack output = itemHandler.getStackInSlot(1);

        if (!bottle.isEmpty() && output.getCount() < output.getMaxStackSize()) {
            bottle.shrink(1);
            ItemStack ampoule = createKCorpAmpule();
            if (output.isEmpty()) {
                itemHandler.setStackInSlot(1, ampoule);
            } else {
                output.grow(1);
            }

            if (level != null) {
                level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.2F);
            }
            setChanged();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KCorpInjectorBlockEntity entity) {
        if (level.getGameTime() % 10 == 0) {
            AABB healingBox = new AABB(pos).inflate(1.2D, 1.8D, 1.2D);
            List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, healingBox);

            if (!nearbyPlayers.isEmpty()) {
                for (Player player : nearbyPlayers) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 1, true, false));
                    player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 15, 0, true, false));
                }

                if (level instanceof ServerLevel serverLevel) {
                    double px = pos.getX() + 0.5D;
                    double py = pos.getY() + 0.9D;
                    double pz = pos.getZ() + 0.5D;
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 8, 0.4D, 0.4D, 0.4D, 0.05D);
                    serverLevel.sendParticles(ParticleTypes.GLOW, px, py, pz, 6, 0.3D, 0.3D, 0.3D, 0.02D);
                }

                if (--entity.sprayCooldown <= 0) {
                    entity.sprayCooldown = 3;
                    level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.5F, 1.4F);
                }
            }
        }
    }

    public static ItemStack createKCorpAmpule() {
        ItemStack potion = new ItemStack(Items.POTION);
        CompoundTag tag = potion.getOrCreateTag();
        tag.putInt("CustomPotionColor", 0x00E676);

        ListTag customEffects = new ListTag();
        CompoundTag regen = new CompoundTag();
        regen.putByte("Id", (byte) 10);
        regen.putByte("Amplifier", (byte) 2);
        regen.putInt("Duration", 400);
        regen.putBoolean("ShowParticles", true);
        customEffects.add(regen);

        CompoundTag heal = new CompoundTag();
        heal.putByte("Id", (byte) 6);
        heal.putByte("Amplifier", (byte) 1);
        heal.putInt("Duration", 1);
        heal.putBoolean("ShowParticles", true);
        customEffects.add(heal);

        tag.put("CustomPotionEffects", customEffects);

        CompoundTag display = new CompoundTag();
        display.putString("Name", Component.Serializer.toJson(Component.literal("§a§l【K公司再生安瓿】")));
        ListTag lore = new ListTag();
        lore.add(net.minecraft.nbt.StringTag.valueOf(Component.Serializer.toJson(Component.literal("§7由都市南部之翼——K公司奇点「眼泪」提炼的微纳米生物原液。"))));
        lore.add(net.minecraft.nbt.StringTag.valueOf(Component.Serializer.toJson(Component.literal("§e瞬间超速再生致命伤势，重构肉体细胞。"))));
        display.put("Lore", lore);
        tag.put("display", display);

        return potion;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.kcorp_injector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new KCorpInjectorMenu(containerId, playerInventory, this, this.data);
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
