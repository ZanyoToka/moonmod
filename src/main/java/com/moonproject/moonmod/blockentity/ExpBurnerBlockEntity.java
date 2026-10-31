package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.block.ExpBurnerBlock;
import com.moonproject.moonmod.gui.ExpBurnerMenu;
import com.moonproject.moonmod.init.ModBlockEntities;
import com.moonproject.moonmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 蓝色烧书熔炉（方块实体）：
 * 支持焚烧原版常规燃料、普通书籍、附魔书、书与笔以及模组异想体碎片。
 * 将蕴含在书页与燃料中的知识与能量转化为纯净的经验值储存于内部。
 */
public class ExpBurnerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MAX_EXP_CAPACITY = 10000; // 经验储存上限

    // 1. 物品槽位（1 个燃料/书籍槽）
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return getFuelBurnDuration(stack) > 0;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    // 2. 燃烧状态与经验存储字段
    private int litTime = 0;          // 当前剩余燃烧时间（tick）
    private int maxLitTime = 0;       // 当前投入物的总燃烧时间（用于 GUI 燃烧高度百分比）
    private int expRate = 1;          // 当前燃料每秒转化的经验值
    private int storedExp = 0;        // 内部累计存储的经验值

    // 3. 服务端与客户端 GUI 数据同步通道
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ExpBurnerBlockEntity.this.litTime;
                case 1 -> ExpBurnerBlockEntity.this.maxLitTime;
                case 2 -> ExpBurnerBlockEntity.this.storedExp;
                case 3 -> ExpBurnerBlockEntity.this.expRate;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ExpBurnerBlockEntity.this.litTime = value;
                case 1 -> ExpBurnerBlockEntity.this.maxLitTime = value;
                case 2 -> ExpBurnerBlockEntity.this.storedExp = value;
                case 3 -> ExpBurnerBlockEntity.this.expRate = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public ExpBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXP_BURNER.get(), pos, state);
    }

    public boolean isLit() {
        return this.litTime > 0;
    }

    public int getStoredExp() {
        return this.storedExp;
    }

    public int getExpRate() {
        return this.expRate;
    }

    /**
     * 计算物品作为烧书熔炉燃料的燃烧时长（tick）
     */
    public static int getFuelBurnDuration(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        // 附魔书：基础 600 tick + 每个附魔等级 200 tick
        if (stack.is(Items.ENCHANTED_BOOK)) {
            int totalEnchLevels = 0;
            for (int lvl : EnchantmentHelper.getEnchantments(stack).values()) {
                totalEnchLevels += lvl;
            }
            return 600 + Math.min(2400, totalEnchLevels * 200);
        }

        // 成书 / 书与笔：600 tick（30 秒）
        if (stack.is(Items.WRITTEN_BOOK) || stack.is(Items.WRITABLE_BOOK)) {
            return 600;
        }

        // 原版书：400 tick（20 秒）
        if (stack.is(Items.BOOK)) {
            return 400;
        }

        // 纸张：100 tick（5 秒）
        if (stack.is(Items.PAPER)) {
            return 100;
        }

        // 模组材料：异想体碎片 800 tick（40 秒）
        if (stack.is(ModItems.ABNORMALITY_FRAGMENT.get())) {
            return 800;
        }

        // 原版常规燃料（煤炭、木炭、岩浆桶等）
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    /**
     * 计算物品每秒产出的经验值速率
     */
    public static int getFuelExpRate(ItemStack stack) {
        if (stack.isEmpty()) return 1;

        if (stack.is(Items.ENCHANTED_BOOK)) {
            int totalEnchLevels = 0;
            for (int lvl : EnchantmentHelper.getEnchantments(stack).values()) {
                totalEnchLevels += lvl;
            }
            // 附魔书提供极高经验产率（每级附魔 +5 EXP/秒，最低 8 EXP/秒）
            return Math.max(8, 8 + totalEnchLevels * 5);
        }

        if (stack.is(Items.WRITTEN_BOOK) || stack.is(Items.WRITABLE_BOOK)) {
            return 6;
        }

        if (stack.is(Items.BOOK)) {
            return 4;
        }

        if (stack.is(Items.PAPER)) {
            return 2;
        }

        if (stack.is(ModItems.ABNORMALITY_FRAGMENT.get())) {
            return 10;
        }

        return 1; // 常规燃料默认 1 点经验/秒
    }

    /**
     * 玩家提取经验：将内部存储的经验全部给玩家并播放音效
     */
    public int extractExp(Player player) {
        int toExtract = this.storedExp;
        if (toExtract > 0) {
            player.giveExperiencePoints(toExtract);
            this.storedExp = 0;
            setChanged();

            // 播放清脆的经验球拾取音效与升级音效
            if (this.level != null) {
                this.level.playSound(null, this.worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.9F, 1.0F);
                if (toExtract >= 50) {
                    this.level.playSound(null, this.worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.2F);
                }
            }
        }
        return toExtract;
    }

    /**
     * 服务端主 Tick 逻辑
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ExpBurnerBlockEntity entity) {
        boolean wasLit = entity.isLit();
        boolean changed = false;

        // 燃烧中：时间逐 tick 减少
        if (entity.isLit()) {
            entity.litTime--;
        }

        // 没在燃烧且经验未满时：尝试消耗燃料点火
        if (!entity.isLit() && entity.storedExp < MAX_EXP_CAPACITY) {
            ItemStack fuelStack = entity.itemHandler.getStackInSlot(0);
            if (!fuelStack.isEmpty()) {
                int burnDuration = getFuelBurnDuration(fuelStack);
                if (burnDuration > 0) {
                    entity.litTime = burnDuration;
                    entity.maxLitTime = burnDuration;
                    entity.expRate = getFuelExpRate(fuelStack);

                    // 处理容器返还（例如熔岩桶用完变成空铁桶）
                    ItemStack containerItem = fuelStack.getCraftingRemainingItem();
                    fuelStack.shrink(1);
                    if (fuelStack.isEmpty()) {
                        entity.itemHandler.setStackInSlot(0, containerItem);
                    }
                    changed = true;
                }
            }
        }

        // 燃烧中且未达上限：每秒（20 tick）转化产生经验
        if (entity.isLit() && entity.storedExp < MAX_EXP_CAPACITY) {
            if (level.getGameTime() % 20 == 0) {
                entity.storedExp = Math.min(MAX_EXP_CAPACITY, entity.storedExp + entity.expRate);
                changed = true;
            }
        }

        // 当点火状态改变时，同步更新世界中方块的 LIT 状态（切换点亮/熄灭贴图与光源）
        if (wasLit != entity.isLit()) {
            level.setBlock(pos, state.setValue(ExpBurnerBlock.LIT, entity.isLit()), 3);
            changed = true;
        }

        if (changed) {
            entity.setChanged();
        }
    }

    // ----------------- MenuProvider 接口实现 -----------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.moonproject.exp_burner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ExpBurnerMenu(containerId, inventory, this, this.dataAccess);
    }

    // ----------------- 存档读写（NBT 持久化） -----------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("BurnTime", this.litTime);
        tag.putInt("MaxBurnTime", this.maxLitTime);
        tag.putInt("StoredExp", this.storedExp);
        tag.putInt("ExpRate", this.expRate);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        this.litTime = tag.getInt("BurnTime");
        this.maxLitTime = tag.getInt("MaxBurnTime");
        this.storedExp = tag.getInt("StoredExp");
        this.expRate = tag.contains("ExpRate") ? tag.getInt("ExpRate") : 1;
    }

    // ----------------- Capability 支持（自动化与槽位交互） -----------------

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
}
