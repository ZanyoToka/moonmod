package com.moonproject.moonmod.item;

import com.moonproject.moonmod.config.MoonProjectConfig;
import com.moonproject.moonmod.event.CombatEvents;
import com.moonproject.moonmod.init.ModParticles;
import com.moonproject.moonmod.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
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
 * <p>步骤 3（设计规格书 4.5）已落地：出鞘/归鞘音效、记忆灼烧的爆裂声与余烬、命中与
 * 大招的破空声和碎屑，见 {@link #playSwitchEffects} 与 {@code CombatEvents#playSlashEffects}。</p>
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

        // 步骤 3（设计规格书 4.5）：形态切换的音效与粒子。
        // 只在服务端触发，playSound / sendParticles 会由服务端同步给附近客户端；
        // 若在客户端也播一次，单机会听到两遍。
        if (!level.isClientSide) {
            playSwitchEffects(level, player, isDrawn(stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * 拔刀 / 收刀的表现层：一声金属音 + （拔刀时）一圈向上的记忆余烬。
     *
     * <p>粒子用 {@code sendParticles} 广播，不写入存档，纯表现。</p>
     */
    private static void playSwitchEffects(Level level, Player player, boolean drawn) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                drawn ? ModSounds.ALAYA_DRAW.get() : ModSounds.ALAYA_SHEATH.get(),
                SoundSource.PLAYERS, 1.0F, drawn ? 1.0F : 0.95F);

        if (!drawn) {
            return;
        }
        // 一圈余烬从脚边升起，表示「记忆开始被烧」这件事已经开始了
        final int count = 24;
        for (int i = 0; i < count; i++) {
            double angle = i * (Math.PI * 2.0D / count);
            double px = player.getX() + Math.cos(angle) * 0.65D;
            double pz = player.getZ() + Math.sin(angle) * 0.65D;
            double py = player.getY() + 0.2D + player.getRandom().nextDouble() * 0.7D;
            serverLevel.sendParticles(ModParticles.MEMORY_EMBER.get(), px, py, pz, 1,
                    Math.cos(angle) * 0.03D, 0.07D, Math.sin(angle) * 0.03D, 0.0D);
        }
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
            if (entity instanceof Player player) {
                if (!burnMemory(player)) {
                    // 记忆被斩空了 → 强制收回鞘中（连"想拔刀"这件事都记不起来）
                    setSheathed(stack);
                    tag.putInt(TAG_MEMORY_TIMER, 0);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.ALAYA_SHEATH.get(), SoundSource.PLAYERS, 0.8F, 0.85F);
                    }
                    return;
                }
                // 每烧掉一截记忆：细碎爆裂声 + 几点余烬，提示"代价还在付"
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.ALAYA_MEMORY_BURN.get(), SoundSource.PLAYERS, 0.55F, 1.0F);
                    serverLevel.sendParticles(ModParticles.MEMORY_EMBER.get(),
                            player.getX(), player.getY() + 1.0D, player.getZ(), 4,
                            0.28D, 0.4D, 0.28D, 0.01D);
                }
            }
        }
        tag.putInt(TAG_MEMORY_TIMER, burnTimer);

        // ── 解放倒计时 ──
        int left = tag.getInt(TAG_DRAW_TICKS) - 1;
        if (left <= 0) {
            setSheathed(stack);
            if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.ALAYA_SHEATH.get(), SoundSource.PLAYERS, 0.7F, 0.8F);
            }
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

            int skillCooldown = tag == null ? 0 : tag.getInt(TAG_SKILL_COOLDOWN);
            if (skillCooldown > 0) {
                tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.cooldown",
                        skillCooldown / 20).withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.ready")
                        .withStyle(ChatFormatting.GOLD));
            }
        }

        // 技能机制：把「怎么出鞘、技能吃什么资源、代价是什么」直接写在简介里，
        // 数值全部读 Config，改配置后简介会自动跟着变。
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.title")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.draw",
                        MoonProjectConfig.drawDurationTicks() / 20)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.heavenly",
                        MoonProjectConfig.heavenlyKillCutbondCost(), (int) MoonProjectConfig.skill3Range())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.erase",
                        MoonProjectConfig.eraseCutbondCost(), (int) MoonProjectConfig.skill3Range())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.cutbond",
                        MoonProjectConfig.cutbondMaxStacks())
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.moonproject.alaya_vijnana.skill.memory",
                        MoonProjectConfig.memoryBurnIntervalTicks() / 20,
                        MoonProjectConfig.memoryCostPerBurn())
                .withStyle(ChatFormatting.DARK_GRAY));

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
