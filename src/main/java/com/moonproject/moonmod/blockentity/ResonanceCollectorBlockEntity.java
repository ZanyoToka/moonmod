package com.moonproject.moonmod.blockentity;

import com.moonproject.moonmod.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 场谐振能源采集单元的方块实体。
 *
 * <p>槽位：0 = 异想体核心（不消耗），1 = 脑啡肽产出，2 = 异想体饰品产出。</p>
 *
 * <p>工作流程：点界面上的四个按钮之一 → 开始 35 秒工作 → 完成后按工作方式产出
 * 脑啡肽放进槽位 1；另有 3% 概率产出饰品、1% 概率生成异想体（后续接入）。</p>
 */
public class ResonanceCollectorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_CORE = 0;
    public static final int SLOT_ENKEPHALIN = 1;
    public static final int SLOT_TRINKET = 2;
    public static final int SLOTS = 3;

    /** 工作总时长：35 秒。 */
    public static final int WORK_TOTAL = 20 * 35;

    /** 工作方式：0=压迫，1=沟通，2=洞察，3=本能。 */
    public static final String[] WORK_NAMES = {"压迫", "沟通", "洞察", "本能"};

    private final SimpleContainer items = new SimpleContainer(SLOTS);

    private int workType = -1;
    private int workProgress = 0;

    public ResonanceCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_COLLECTOR.get(), pos, state);
    }

    public SimpleContainer getItems() {
        return items;
    }

    public int getWorkType() {
        return workType;
    }

    public int getWorkProgress() {
        return workProgress;
    }

    public boolean isWorking() {
        return workType >= 0;
    }

    /** 开始工作：核心槽为空时不能开始。 */
    public boolean startWork(int type) {
        if (type < 0 || type > 3 || isWorking()) {
            return false;
        }
        if (items.getItem(SLOT_CORE).isEmpty()) {
            return false;
        }
        this.workType = type;
        this.workProgress = 0;
        setChanged();
        return true;
    }

    /** 每 tick 由 {@link #serverTick} 调用。 */
    private void tickWork(Level level) {
        if (!isWorking()) {
            return;
        }
        workProgress++;
        if (workProgress >= WORK_TOTAL) {
            int type = workType;
            workType = -1;
            workProgress = 0;
            finishWork(level, type);
            setChanged();
        }
    }

    /** 每次工作产出饰品的基础概率。 */
    public static final float TRINKET_CHANCE = 0.03F;
    /** 每次工作生成异想体的基础概率。 */
    public static final float ABNORMALITY_CHANCE = 0.01F;

    /**
     * 工作结束结算。
     *
     * <ol>
     *   <li>按工作方式产出脑啡肽（1~4 个，后续接核心倾向数据后细化）</li>
     *   <li>3% 概率产出异想体饰品到槽位 2（饰品物品后续实现，先预留）</li>
     *   <li>1% 概率在方块上方生成对应异想体（实体后续实现，先播粒子提示）</li>
     * </ol>
     */
    /**
     * 计算本次工作的脑啡肽产出（0~30，含端点）。
     *
     * <p>当前是占位实现：以工作方式为基准做随机浮动，保证结果落在 [0, 30]。
     * 接入异想体核心数据后，改由「核心对各工作方式的倾向表」决定分布。</p>
     */
    private int rollEnkephalin(Level level, int type) {
        // 四种工作方式目前产出完全一致：0~30 均匀随机
        // 后续由异想体核心的「倾向表」决定各方式的期望值与分布
        return level.getRandom().nextInt(31);
    }

    private void finishWork(Level level, int type) {
        // ① 脑啡肽：单次产出 0~30（含端点），具体分布将来由「异想体核心 + 工作方式」的倾向数据决定
        int amount = rollEnkephalin(level, type);
        ItemStack out = items.getItem(SLOT_ENKEPHALIN);
        if (out.isEmpty()) {
            items.setItem(SLOT_ENKEPHALIN, new ItemStack(
                    com.moonproject.moonmod.item.ModItems.ENKEPHALIN.get(), amount));
        } else if (out.is(com.moonproject.moonmod.item.ModItems.ENKEPHALIN.get())) {
            out.grow(amount);
        }

        // ② 3% 饰品（物品未实现前先只放提示）
        if (level.getRandom().nextFloat() < TRINKET_CHANCE) {
            // TODO: 换成对应异想体的饰品物品
            // items.setItem(SLOT_TRINKET, new ItemStack(ModItems.XXX_TRINKET.get()));
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5,
                        8, 0.3, 0.2, 0.3, 0.0);
            }
        }

        // ③ 1% 生成异想体（实体未实现前先用粒子+广播提示）
        if (level.getRandom().nextFloat() < ABNORMALITY_CHANCE) {
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5,
                        30, 0.5, 0.5, 0.5, 0.02);
                for (net.minecraft.world.entity.player.Player p : server.getPlayers(p ->
                        p.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 400)) {
                    p.displayClientMessage(net.minecraft.network.chat.Component.literal(
                            "§5异常反应：异想体正在显现……"), true);
                }
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonanceCollectorBlockEntity be) {
        be.tickWork(level);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag slot = new CompoundTag();
                stack.save(slot);
                tag.put("Slot" + i, slot);
            }
        }
        tag.putInt("WorkType", workType);
        tag.putInt("WorkProgress", workProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < SLOTS; i++) {
            String key = "Slot" + i;
            items.setItem(i, tag.contains(key) ? ItemStack.of(tag.getCompound(key)) : ItemStack.EMPTY);
        }
        workType = tag.getInt("WorkType");
        workProgress = tag.getInt("WorkProgress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.moonproject.resonance_collector");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
        return new com.moonproject.moonmod.gui.ResonanceCollectorMenu(windowId, inv, this);
    }
}