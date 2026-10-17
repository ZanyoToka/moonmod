package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.gui.ExpBurnerMenu;
import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
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

public class ExpBurnerBlockEntity extends BlockEntity implements MenuProvider {

    // 1. 物品槽位（1 个燃料槽）
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    // 2. 燃烧状态与经验存储字段
    private int litTime = 0;       // 当前剩余燃烧时间（tick）
    private int maxLitTime = 0;    // 当前燃料的总燃烧时间（用于计算 GUI 火焰高度百分比）
    private int storedExp = 0;     // 内部累计存储的经验值
    private static final int MAX_EXP_CAPACITY = 10000; // 经验储存上限

    // 3. 服务端与客户端 GUI 数据同步通道
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ExpBurnerBlockEntity.this.litTime;
                case 1 -> ExpBurnerBlockEntity.this.maxLitTime;
                case 2 -> ExpBurnerBlockEntity.this.storedExp;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ExpBurnerBlockEntity.this.litTime = value;
                case 1 -> ExpBurnerBlockEntity.this.maxLitTime = value;
                case 2 -> ExpBurnerBlockEntity.this.storedExp = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
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

    /**
     * 玩家点击提取或 Shift 右键时调用：提取全部内部经验
     */
    public int extractExp(Player player) {
        int toExtract = this.storedExp;
        if (toExtract > 0) {
            player.giveExperiencePoints(toExtract);
            this.storedExp = 0;
            setChanged();
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
                int burnDuration = ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
                if (burnDuration > 0) {
                    entity.litTime = burnDuration;
                    entity.maxLitTime = burnDuration; // 记录上限用于 GUI 火焰比例

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

        // 燃烧中且未达上限：每秒（20 tick）转化产生 1 点经验
        if (entity.isLit() && entity.storedExp < MAX_EXP_CAPACITY) {
            if (level.getGameTime() % 20 == 0) {
                entity.storedExp++;
                changed = true;
            }
        }

        // 标记区块存盘
        if (changed || wasLit != entity.isLit()) {
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
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        this.litTime = tag.getInt("BurnTime");
        this.maxLitTime = tag.getInt("MaxBurnTime");
        this.storedExp = tag.getInt("StoredExp");
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