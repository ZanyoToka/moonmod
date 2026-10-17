package com.moonproject.moonmod.item;

import com.moonproject.moonmod.config.MoonProjectConfig;
import com.moonproject.moonmod.event.CombatEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 天杀星刀·阿赖耶识。
 *
 * <p>本类只负责「形态」这层：鞘击（默认，刀锁在鞘中）与拔刀解放（限时）。
 * 形态决定的面板伤害与攻速由 {@code AlayaAttributeHandler} 通过
 * {@code ItemAttributeModifierEvent} 按 NBT 动态下发，所以数值全部来自 Config。</p>
 *
 * <p>尚未实现（见设计规格书第 7.4 节）：记忆代价、焦黑斩痕、概念抹除、粒子音效。</p>
 */
public class AlayaVijnanaItem extends SwordItem {

    /** 是否处于解放态。 */
    public static final String TAG_DRAWN = "AlayaDrawn";
    /** 解放态剩余 tick。 */
    public static final String TAG_DRAW_TICKS = "AlayaDrawTicks";
    /** 形态切换冷却剩余 tick。 */
    public static final String TAG_COOLDOWN = "AlayaSwitchCooldown";
    /** 侵蚀值（步骤 2/机制 5 使用）。 */
    public static final String TAG_EROSION = "AlayaErosion";
    /** 记忆灼烧计时：解放期间每 memoryBurnIntervalTicks 扣一次（设计规格书 6.4）。 */
    public static final String TAG_MEMORY_TIMER = "AlayaMemoryTimer";
    /** 自动追击（突进）冷却剩余 tick。 */
    public static final String TAG_LUNGE_COOLDOWN = "AlayaLungeCooldown";
    /** 右键技能（描绘 / 抹去）的冷却剩余 tick。 */
    public static final String TAG_SKILL_COOLDOWN = "AlayaSkillCooldown";

    public AlayaVijnanaItem(Item.Properties properties) {
        super(AlayaTier.INSTANCE,
                (int) MoonProjectConfig.DEFAULT_SHEATHED_DAMAGE,
                (float) MoonProjectConfig.DEFAULT_SHEATHED_ATTACK_SPEED,
                properties);
    }

    public static boolean isDrawn(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_DRAWN);
    }

    /** 是否为「手中正握着的解放态阿赖耶识」——战斗事件里反复用到这个判断。 */
    public static boolean isDrawnBlade(ItemStack stack) {
        return stack.getItem() instanceof AlayaVijnanaItem && isDrawn(stack);
    }

    public static void setSheathed(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_DRAWN, false);
        tag.putInt(TAG_DRAW_TICKS, 0);
    }

    public static void setDrawn(ItemStack stack, int durationTicks) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_DRAWN, true);
        tag.putInt(TAG_DRAW_TICKS, durationTicks);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        // 潜行 + 右键 = 描绘 / 强化大招「将我抹去，也将你抹去。」（仅拔刀态）
        if (player.isShiftKeyDown() && tag.getBoolean(TAG_DRAWN)) {
            if (!level.isClientSide) {
                CombatEvents.castConceptSever(level, player, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (tag.getInt(TAG_COOLDOWN) > 0) {
            return InteractionResultHolder.fail(stack);
        }

        if (tag.getBoolean(TAG_DRAWN)) {
            setSheathed(stack);
        } else {
            // 拔刀前先确认付得起记忆代价，否则拔出来也会在下一秒被强制收回
            if (!canAffordDraw(player)) {
                if (!level.isClientSide) {
                    player.displayClientMessage(memoryEmptyMessage(), true);
                }
                return InteractionResultHolder.fail(stack);
            }
            setDrawn(stack, MoonProjectConfig.drawDurationTicks());
        }
        stack.getOrCreateTag().putInt(TAG_COOLDOWN, MoonProjectConfig.switchCooldownTicks());

        // 粒子与音效在步骤 3 补上（设计规格书 4.5）。
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        int cooldown = tag.getInt(TAG_COOLDOWN);
        if (cooldown > 0) {
            tag.putInt(TAG_COOLDOWN, cooldown - 1);
        }

        // 突进与技能3的冷却（与形态无关，始终计时）
        int lunge = tag.getInt(TAG_LUNGE_COOLDOWN);
        if (lunge > 0) {
            tag.putInt(TAG_LUNGE_COOLDOWN, lunge - 1);
        }
        int skill = tag.getInt(TAG_SKILL_COOLDOWN);
        if (skill > 0) {
            tag.putInt(TAG_SKILL_COOLDOWN, skill - 1);
        }

        if (!tag.getBoolean(TAG_DRAWN)) {
            tag.putInt(TAG_MEMORY_TIMER, 0);
            return;
        }

        // ── 记忆代价（设计规格书 6.4）：解放期间定期烧掉使用者的记忆 ──
        int burnTimer = tag.getInt(TAG_MEMORY_TIMER) + 1;
        int burnInterval = Math.max(1, MoonProjectConfig.memoryBurnIntervalTicks());
        if (burnTimer >= burnInterval) {
            burnTimer = 0;
            if (entity instanceof Player player && !burnMemory(player)) {
                // 记忆被斩空了 → 强制收回鞘中（连"想拔刀"这件事都记不起来）
                setSheathed(stack);
                tag.putInt(TAG_MEMORY_TIMER, 0);
                return;
            }
        }
        tag.putInt(TAG_MEMORY_TIMER, burnTimer);

        // ── 解放倒计时 ──
        int left = tag.getInt(TAG_DRAW_TICKS) - 1;
        if (left <= 0) {
            setSheathed(stack);
        } else {
            tag.putInt(TAG_DRAW_TICKS, left);
        }
    }

    /**
     * 是否付得起拔刀的代价。
     *
     * <p>创造 / 旁观模式直接放行——这两类玩家的经验等级恒为 0，
     * 否则在创造模式里会永远拔不出刀（玩家实测反馈的缺陷）。</p>
     */
    public static boolean canAffordDraw(Player player) {
        int cost = MoonProjectConfig.memoryCostPerBurn();
        if (cost <= 0 || player.isCreative() || player.isSpectator()) {
            return true;
        }
        return player.experienceLevel >= cost;
    }

    private static Component memoryEmptyMessage() {
        return Component.translatable("item.moonproject.alaya_vijnana.memory_empty")
                .withStyle(ChatFormatting.DARK_RED);
    }

    /**
     * 烧掉一次记忆（打样期以经验等级计）。
     *
     * @return false 表示记忆已经空了，调用方应强制收刀
     */
    private static boolean burnMemory(Player player) {
        int cost = MoonProjectConfig.memoryCostPerBurn();
        if (cost <= 0) {
            return true; // 配置成 0 = 关闭记忆代价
        }
        if (player.isCreative() || player.isSpectator()) {
            return true; // 创造/旁观模式不消耗记忆
        }
        if (player.experienceLevel < cost) {
            player.displayClientMessage(memoryEmptyMessage(), true);
            return false;
        }
        player.giveExperienceLevels(-cost);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean drawn = isDrawn(stack);
        tooltip.add(Component.translatable(drawn
                        ? "item.moonproject.alaya_vijnana.state.drawn"
                        : "item.moonproject.alaya_vijnana.state.sheathed")
                .withStyle(drawn ? ChatFormatting.WHITE : ChatFormatting.GRAY));

        if (drawn) {
            CompoundTag tag = stack.getTag();
            int seconds = tag == null ? 0 : Math.max(0, tag.getInt(TAG_DRAW_TICKS) / 20);
            tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.remaining", seconds)
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.memory")
                    .withStyle(ChatFormatting.RED));

            int skillCooldown = tag == null ? 0 : tag.getInt(TAG_SKILL_COOLDOWN);
            if (skillCooldown > 0) {
                tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.cooldown",
                        skillCooldown / 20).withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.ready")
                        .withStyle(ChatFormatting.GOLD));
            }
        }

        tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.hint")
                .withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, level, tooltip, flag);
    }

    /**
     * 自定义 Tier：耐久 2500、附魔性 22，攻击力加成固定为 0——
     * 伤害完全由属性修正决定，避免与 Config 数值叠加。
     */
    public enum AlayaTier implements Tier {
        INSTANCE;

        @Override
        public int getLevel() {
            return 3;
        }

        @Override
        public int getUses() {
            return 2500;
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.0F;
        }

        @Override
        public int getEnchantmentValue() {
            return 22;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.EGO_CORE.get());
        }
    }
}
