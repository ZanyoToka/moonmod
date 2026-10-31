package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.block.IdentityExtractorBlock;
import com.moonproject.moonmod.init.ModBlockEntities;
import com.moonproject.moonmod.item.ModItems;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
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

import java.util.List;

/**
 * 【人格抽取共鸣舱】方块实体 (Identity Extractor)
 * <p>利用墨菲斯托脑啡肽裂解原理与镜面技术，共鸣并具象化异界可能性的机器。
 * 能够接收异想体碎片、E.G.O核心、脑啡肽（附魔之瓶）及紫水晶镜面碎片等进行高维共鸣抽取。</p>
 */
public class IdentityExtractorBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {

    public static final int TOTAL_EXTRACT_TIME = 60; // 抽取耗时 60 ticks (3秒)

    // 0: 输入媒介槽位, 1: 抽取完成产物暂存槽
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return isExtractFuel(stack);
            }
            return false;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private int extractProgress = 0;
    private boolean isExtracting = false;
    private ItemStack activeMedium = ItemStack.EMPTY;

    protected final net.minecraft.world.inventory.ContainerData data = new net.minecraft.world.inventory.ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> extractProgress;
                case 1 -> TOTAL_EXTRACT_TIME;
                case 2 -> isExtracting ? 1 : 0;
                case 3 -> !itemHandler.getStackInSlot(0).isEmpty() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                extractProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public void triggerExtraction(net.minecraft.world.entity.player.Player player) {
        if (!isExtracting && !itemHandler.getStackInSlot(0).isEmpty()) {
            ItemStack stack = itemHandler.extractItem(0, 1, false);
            startExtraction(stack);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.identity_extractor");
    }

    @Nullable
    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, net.minecraft.world.entity.player.Player player) {
        return new com.moonproject.moonmod.gui.IdentityExtractorMenu(containerId, playerInventory, this, this.data);
    }

    public IdentityExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IDENTITY_EXTRACTOR.get(), pos, state);
    }

    public static boolean isExtractFuel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ModItems.ABNORMALITY_FRAGMENT.get())
                || stack.is(ModItems.EGO_CORE.get())
                || stack.is(Items.EXPERIENCE_BOTTLE)
                || stack.is(Items.AMETHYST_SHARD)
                || stack.is(Items.ENCHANTED_BOOK)
                || stack.is(Items.BOOK)
                || stack.is(Items.NETHER_STAR);
    }

    public boolean isExtracting() {
        return isExtracting;
    }

    public int getExtractProgress() {
        return extractProgress;
    }

    /**
     * 投入材料并启动共鸣抽取流程
     */
    public boolean startExtraction(ItemStack medium) {
        if (isExtracting || !isExtractFuel(medium)) {
            return false;
        }

        this.activeMedium = medium.split(1);
        this.isExtracting = true;
        this.extractProgress = 0;
        setChanged();

        if (level != null) {
            // 同步方块状态为抽取中
            BlockState currentState = level.getBlockState(worldPosition);
            if (currentState.hasProperty(IdentityExtractorBlock.EXTRACTING)) {
                level.setBlock(worldPosition, currentState.setValue(IdentityExtractorBlock.EXTRACTING, true), 3);
            }

            // 启动音效：镜面碎裂与硬币掷出
            level.playSound(null, worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.2F, 0.7F);
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.5F, 1.1F);
            level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 0.5F);
        }

        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IdentityExtractorBlockEntity entity) {
        if (!entity.isExtracting) {
            return;
        }

        entity.extractProgress++;

        // 抽取进行中的粒子与音效（每 5 tick 播散紫黑色与苍白色镜面裂痕粒子）
        if (entity.extractProgress % 5 == 0 && level instanceof ServerLevel serverLevel) {
            double px = pos.getX() + 0.5D;
            double py = pos.getY() + 0.7D;
            double pz = pos.getZ() + 0.5D;

            // 紫黑色深渊粒子（Portal & Dragon Breath）
            serverLevel.sendParticles(ParticleTypes.PORTAL, px, py, pz, 6, 0.25D, 0.25D, 0.25D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, px, py, pz, 3, 0.2D, 0.2D, 0.2D, 0.02D);
            // 苍白色镜面火花（Soul Fire Flame & Glow）
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 2, 0.15D, 0.15D, 0.15D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.GLOW, px, py, pz, 4, 0.3D, 0.3D, 0.3D, 0.05D);
        }

        // 抽取阶段音效节奏
        if (entity.extractProgress == 20) {
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.2F, 1.0F);
        } else if (entity.extractProgress == 40) {
            level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.4F, 1.5F);
        }

        // 抽取完成判定
        if (entity.extractProgress >= TOTAL_EXTRACT_TIME) {
            entity.completeExtraction();
        }
    }

    /**
     * 抽取完成结算
     */
    private void completeExtraction() {
        if (level == null || level.isClientSide) return;

        this.isExtracting = false;
        this.extractProgress = 0;

        // 恢复方块状态
        BlockState currentState = level.getBlockState(worldPosition);
        if (currentState.hasProperty(IdentityExtractorBlock.EXTRACTING)) {
            level.setBlock(worldPosition, currentState.setValue(IdentityExtractorBlock.EXTRACTING, false), 3);
        }

        // 结算生成产物
        ItemStack resultStack = generateIdentityReward(this.activeMedium, level);
        this.activeMedium = ItemStack.EMPTY;

        // 弹出至方块上方
        double spawnX = worldPosition.getX() + 0.5D;
        double spawnY = worldPosition.getY() + 1.2D;
        double spawnZ = worldPosition.getZ() + 0.5D;

        ItemEntity droppedItem = new ItemEntity(level, spawnX, spawnY, spawnZ, resultStack);
        droppedItem.setDeltaMovement(0.0D, 0.3D, 0.0D);
        level.addFreshEntity(droppedItem);

        // 成功音效：高阶人格共鸣大成！
        level.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.5F, 1.2F);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 2.0F, 0.8F);
        level.playSound(null, worldPosition, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 0.5F, 1.8F);

        // 金色与镜面火花爆开粒子
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT, spawnX, spawnY, spawnZ, 20, 0.4D, 0.4D, 0.4D, 0.2D);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, spawnX, spawnY, spawnZ, 30, 0.6D, 0.6D, 0.6D, 0.5D);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, spawnX, spawnY, spawnZ, 15, 0.3D, 0.3D, 0.3D, 0.1D);
        }

        setChanged();
    }

    /**
     * 依据投入材料与原著等级体系生成人格卡/书页奖励
     */
    private ItemStack generateIdentityReward(ItemStack medium, Level level) {
        var random = level.random;

        // 1. 如果投入了 E.G.O 核心 或 下界之星 —— 极高概率产出 000 级极上位人格书页 / 高级装备
        if (medium.is(ModItems.EGO_CORE.get()) || medium.is(Items.NETHER_STAR)) {
            int roll = random.nextInt(3);
            if (roll == 0) {
                // 000 级人格·南部第4科科长 附魔之书
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(Enchantments.SHARPNESS, 5));
                EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(Enchantments.UNBREAKING, 3));
                book.setHoverName(Component.literal("§6【000级人格·南部第4科科长】共鸣书页"));
                return book;
            } else if (roll == 1) {
                // 000 级人格·W公司清扫要员 附魔之书
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(Enchantments.ALL_DAMAGE_PROTECTION, 4));
                EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(Enchantments.FALL_PROTECTION, 4));
                book.setHoverName(Component.literal("§d【000级人格·W公司次元清扫要员】共鸣书页"));
                return book;
            } else {
                // 珍贵的核心回馈：附赠大量碎片与后悔武器胚体
                return new ItemStack(ModItems.REGRET.get());
            }
        }

        // 2. 如果投入了异想体碎片 或 紫水晶镜面碎片 —— 产出 00 级收尾人人格书页
        if (medium.is(ModItems.ABNORMALITY_FRAGMENT.get()) || medium.is(Items.AMETHYST_SHARD)) {
            int roll = random.nextInt(3);
            if (roll == 0) {
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(Enchantments.MOB_LOOTING, 3));
                book.setHoverName(Component.literal("§b【00级人格·黑云会若众】共鸣书页"));
                return book;
            } else if (roll == 1) {
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(Enchantments.SWEEPING_EDGE, 3));
                book.setHoverName(Component.literal("§a【00级人格·剑契派门徒】共鸣书页"));
                return book;
            } else {
                // 偶发升格产出 E.G.O 核心！
                return new ItemStack(ModItems.EGO_CORE.get());
            }
        }

        // 3. 常规书籍 / 经验瓶
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(Enchantments.UNBREAKING, 2));
        book.setHoverName(Component.literal("§7【0级人格·都市流浪收尾人】残缺书页"));
        return book;
    }

    public void dropInventory() {
        if (level != null && !level.isClientSide) {
            if (!activeMedium.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), activeMedium);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (!lazyItemHandler.isPresent()) {
                lazyItemHandler = LazyOptional.of(() -> itemHandler);
            }
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("ExtractProgress", extractProgress);
        tag.putBoolean("IsExtracting", isExtracting);
        if (!activeMedium.isEmpty()) {
            tag.put("ActiveMedium", activeMedium.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        extractProgress = tag.getInt("ExtractProgress");
        isExtracting = tag.getBoolean("IsExtracting");
        if (tag.contains("ActiveMedium")) {
            activeMedium = ItemStack.of(tag.getCompound("ActiveMedium"));
        } else {
            activeMedium = ItemStack.EMPTY;
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
