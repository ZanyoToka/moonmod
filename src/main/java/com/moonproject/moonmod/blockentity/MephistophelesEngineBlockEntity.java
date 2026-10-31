package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.block.MephistophelesEngineBlock;
import com.moonproject.moonmod.block.MephistophelesSeatBlock;
import com.moonproject.moonmod.gui.MephistophelesEngineMenu;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
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
 * 【梅菲斯特引擎核心】方块实体 (Mephistopheles Engine Core)
 * <p>边狱巴士的生物柴油引擎核心。能够吞噬有机残渣（腐肉、骨头、生肉、生物组织）并产生高密度异想体动力。
 * 协同机制：当引擎运转时，为周围 16 格内坐在「梅菲斯特客席」上的乘员提供强效理智抚平与生命再生光环。</p>
 */
public class MephistophelesEngineBlockEntity extends BlockEntity implements MenuProvider {

    private int fuelTicks = 0;          // 剩余运行时间（ticks）
    private int maxFuelTicks = 0;       // 当前批次总燃烧时长
    private int soundCooldown = 0;      // 引擎心跳低沉轰鸣间隔
    private int nearbySeatsCount = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return getBioFuelValue(stack) > 0;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> fuelTicks;
                case 1 -> maxFuelTicks;
                case 2 -> nearbySeatsCount;
                case 3 -> isLit() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) fuelTicks = value;
            else if (index == 1) maxFuelTicks = value;
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public MephistophelesEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEPHISTOPHELES_ENGINE.get(), pos, state);
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

    public boolean isLit() {
        return fuelTicks > 0;
    }

    public int getFuelTicks() {
        return fuelTicks;
    }

    public static int getBioFuelValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(Items.ROTTEN_FLESH)) return 300;     // 腐肉 15秒
        if (stack.is(Items.BONE)) return 200;             // 骨头 10秒
        if (stack.is(Items.BONE_MEAL)) return 70;         // 骨粉 3.5秒
        if (stack.is(Items.SPIDER_EYE)) return 400;       // 蜘蛛眼 20秒
        if (stack.is(Items.FERMENTED_SPIDER_EYE)) return 600;
        if (stack.is(Items.PORKCHOP) || stack.is(Items.BEEF) || stack.is(Items.MUTTON) || stack.is(Items.CHICKEN)) return 400;
        if (stack.is(ModItems.ABNORMALITY_FRAGMENT.get())) return 1200; // 异想体碎片 60秒
        if (stack.is(ModItems.EGO_CORE.get())) return 6000;             // EGO核心 300秒
        return 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MephistophelesEngineBlockEntity entity) {
        boolean wasLit = entity.isLit();

        // 1. 自动从内部槽位取燃料
        if (entity.fuelTicks <= 0 && !entity.itemHandler.getStackInSlot(0).isEmpty()) {
            ItemStack stack = entity.itemHandler.getStackInSlot(0);
            int fuelVal = getBioFuelValue(stack);
            if (fuelVal > 0) {
                entity.fuelTicks += fuelVal;
                entity.maxFuelTicks = Math.max(entity.maxFuelTicks, entity.fuelTicks);
                stack.shrink(1);
                entity.setChanged();
            }
        }

        // 2. 自动吞噬周围 2.5 格内的生化残渣掉落物
        if (level.getGameTime() % 10 == 0) {
            AABB absorbBox = new AABB(pos).inflate(2.0D);
            List<ItemEntity> droppedItems = level.getEntitiesOfClass(ItemEntity.class, absorbBox);
            for (ItemEntity itemEntity : droppedItems) {
                if (!itemEntity.isAlive()) continue;
                ItemStack stack = itemEntity.getItem();
                int fuelVal = getBioFuelValue(stack);
                if (fuelVal > 0) {
                    entity.fuelTicks += fuelVal;
                    entity.maxFuelTicks = Math.max(entity.maxFuelTicks, entity.fuelTicks);
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    } else {
                        itemEntity.setItem(stack);
                    }

                    level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.8F, 0.6F);
                    level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.6F, 0.5F);

                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, itemEntity.getX(), itemEntity.getY() + 0.2D, itemEntity.getZ(), 10, 0.2D, 0.2D, 0.2D, 0.05D);
                        serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.03D);
                    }
                    entity.setChanged();
                    break;
                }
            }
        }

        // 3. 燃料消耗与客席协同
        if (entity.fuelTicks > 0) {
            entity.fuelTicks--;

            if (--entity.soundCooldown <= 0) {
                entity.soundCooldown = 40;
                level.playSound(null, pos, SoundEvents.MINECART_RIDING, SoundSource.BLOCKS, 0.35F, 0.6F);
                level.playSound(null, pos, SoundEvents.BLAZE_BURN, SoundSource.BLOCKS, 0.4F, 0.5F);
            }

            if (level.getGameTime() % 20 == 0) {
                int seatsCount = 0;
                BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                int r = 16;
                for (int dx = -r; dx <= r; dx += 4) {
                    for (int dy = -4; dy <= 4; dy += 2) {
                        for (int dz = -r; dz <= r; dz += 4) {
                            cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                            if (level.getBlockState(cursor).getBlock() instanceof MephistophelesSeatBlock) {
                                seatsCount++;
                            }
                        }
                    }
                }
                entity.nearbySeatsCount = seatsCount;

                AABB synergyBox = new AABB(pos).inflate(16.0D, 6.0D, 16.0D);
                List<Player> players = level.getEntitiesOfClass(Player.class, synergyBox);
                for (Player player : players) {
                    if (player.isPassenger()) {
                        if (Sanity.hasSanity(player)) {
                            Sanity.add(player, 1);
                        }
                        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, true, false));
                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false));
                    }
                }
            }
        }

        if (wasLit != entity.isLit()) {
            entity.updateLitState(entity.isLit());
            entity.setChanged();
        }
    }

    private void updateLitState(boolean lit) {
        if (level != null) {
            BlockState currentState = level.getBlockState(worldPosition);
            if (currentState.hasProperty(MephistophelesEngineBlock.LIT) && currentState.getValue(MephistophelesEngineBlock.LIT) != lit) {
                level.setBlock(worldPosition, currentState.setValue(MephistophelesEngineBlock.LIT, lit), 3);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.mephistopheles_engine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MephistophelesEngineMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("FuelTicks", fuelTicks);
        tag.putInt("MaxFuelTicks", maxFuelTicks);
        tag.put("Inventory", itemHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuelTicks = tag.getInt("FuelTicks");
        maxFuelTicks = tag.getInt("MaxFuelTicks");
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
