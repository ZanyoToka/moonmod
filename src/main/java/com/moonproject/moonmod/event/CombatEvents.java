package com.moonproject.moonmod.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.config.MoonProjectConfig;
import com.moonproject.moonmod.effect.ModEffects;
import com.moonproject.moonmod.item.ModItems;
import com.moonproject.moonmod.sanity.Sanity;
import com.moonproject.moonmod.item.AlayaVijnanaItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 阿赖耶识的战斗机制。状态按原著，斩击<b>延迟生效</b>。
 *
 * <p>原著依据（2026-10-17 查证）：</p>
 * <ul>
 *   <li>「小指·蜘蛛巢之刃 良秀」：出血 / 火伤 / 呼吸；技能强度由层数总和决定。</li>
 *   <li>9-49「友方-无我良秀」：裂伤 / 斩断 / 斩缘；被动「无我梦中」「支离灭裂」。</li>
 *   <li>原著攻击有前摇、不是瞬间结算 → 见 {@link SlashScheduler}。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class CombatEvents {
    /** 禁疗截止时间（游戏刻）。 */
    public static final String NO_HEAL_UNTIL = "MoonNoHealUntil";
    /** 递归保护：结算类伤害会再次触发命中事件。 */
    private static boolean resolving = false;
    /** 标记"这一下是我们自己延迟落地的伤害"，避免再次进入调度。 */
    private static boolean applyingDelayed = false;

    // ==================== 命中主流程 ====================

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (resolving) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        boolean delayed = applyingDelayed;
        resolving = true;
        try {
            // 原著 DoT：命中时按层数结算并减层
            resolveStackDot(target, ModEffects.BLEED.get(), 1);
            resolveStackDot(target, ModEffects.LACERATION.get(), 2);
            resolveStackDot(target, ModEffects.RUPTURE.get(), 1);   // 通用版（弱化）
            resolveStackDot(target, ModEffects.TREMOR.get(), 1);    // 通用版（弱化）
            // 支离灭裂：斩缘 + 对手斩断 减伤
            applyMitigation(event);

            if (delayed) {
                return; // 我们自己落地的那一下：只做上面的结算
            }

            // 被打中会打断尚未落地的慢刀
            if (target instanceof Player hurtPlayer) {
                SlashScheduler.cancelSlow(hurtPlayer);
            }

            if (event.getSource().getEntity() instanceof Player player) {
                ItemStack stack = player.getMainHandItem();
                if (AlayaVijnanaItem.isDrawnBlade(stack)) {
                    scheduleSlash(player, target, event, stack);
                }
            }
        } finally {
            resolving = false;
        }
    }

    /** 无法愈合：带斩痕的目标，任何治疗回复都被取消。 */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (isNoHeal(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** 存在抹除：带斩痕死亡的生物不留遗物。 */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        // 被阿赖耶识击杀（任意技能）→ 存在抹除：不留任何掉落物
        if (event.getSource().getEntity() instanceof Player killer
                && killer.getMainHandItem().is(ModItems.ALAYA_VIJNANA.get())) {
            event.getDrops().clear();
            return;
        }
        if (isNoHeal(event.getEntity())) {
            event.getDrops().clear();
        }
    }

    /** 斩痕复发 + 驱动延迟斩击的计时。 */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (entity instanceof Player player) {
            SlashScheduler.tick(player);
        }

        MobEffectInstance scar = entity.getEffect(ModEffects.BLACK_SCAR.get());
        if (scar == null || scar.getDuration() > 40) {
            return;
        }
        double radius = MoonProjectConfig.scarReapplyRadius();
        if (radius <= 0.0D) {
            return;
        }
        for (Player player : entity.level().getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(radius))) {
            if (AlayaVijnanaItem.isDrawnBlade(player.getMainHandItem())) {
                applyScar(entity);
                break;
            }
        }
    }

    // ==================== 六个技能（原著数值比 12:12:15:15:24:36）====================

    /** 原著「无我良秀」的六个技能。coins 用多段延迟命表现，ratio 是相对面板伤害的倍率。 */
    private enum Skill {
        SLASH("挥砍", 2, 1.00D, false, 4),
        SMEAR("涂抹", 2, 1.00D, false, 3),
        SPILL("泼洒", 3, 1.25D, false, 5),
        DEPICT("描绘", 3, 1.25D, true, 5),
        HEAVENLY_KILL("天杀", 5, 24.00D, true, 4),
        ERASE("将我抹去，也将你抹去。", 5, 40.00D, true, 5);

        final String display;
        final int coins;
        final double ratio;
        final boolean heavy;
        final int laceration;

        Skill(String display, int coins, double ratio, boolean heavy, int laceration) {
            this.display = display;
            this.coins = coins;
            this.ratio = ratio;
            this.heavy = heavy;
            this.laceration = laceration;
        }
    }

    /**
     * 左键系：左键 = 挥砍；疾跑 + 左键 = 涂抹；潜行 + 左键 = 泼洒；
     * 潜行 + 左键且斩缘 ≥ 阈值 = 大招「天杀」（消耗斩缘）。
     */
    private static Skill pickLeftClickSkill(Player player) {
        if (player.isShiftKeyDown()) {
            // 天杀只通过专属键 R 释放；潜行 + 左键固定为泼洒
            return Skill.SPILL;
        }
        return player.isSprinting() ? Skill.SMEAR : Skill.SLASH;
    }

    private static void scheduleSlash(Player player, LivingEntity target, LivingHurtEvent event, ItemStack stack) {
        Skill skill = pickLeftClickSkill(player);
        float base = event.getAmount();
        event.setCanceled(true); // 即时伤害取消，改为多段延迟落地
        scheduleSkill(player, target, skill, base);
        tryLunge(player, target, stack);
        scheduleSweep(player, target, base * (float) skill.ratio, skill.heavy);
    }

    /** 按技能把伤害拆成「硬币数」段，错开 2 tick 依次落地。 */
    private static void scheduleSkill(Player player, LivingEntity target, Skill skill, float baseDamage) {
        int poise = stackCount(player, ModEffects.POISE.get());
        double multiplier = skill.ratio * (1.0D + poise * MoonProjectConfig.slowSlashPoiseBonus());
        float total = (float) (baseDamage * multiplier);
        // 原著「硬币系统」：每个技能有若干枚硬币，每枚随机正面（威力 +20%）或反面（威力 -20%）
        // 原著「硬币系统」：按「正面数 / 硬币总数」的比例计算倍率
        //   倍率 = 0.2 + 1.8 * (好硬币数 / 硬币总数)   -> 全坏 0.2，全好 2.0
        final boolean inverted = Sanity.hasAbyssCall(player);
        final int sanity = Sanity.get(player);
        final double headsChance = Math.max(0.05D, Math.min(0.95D, 0.5D + sanity / 200.0D));
        int goodCount = 0;
        StringBuilder faces = new StringBuilder();
        for (int i = 0; i < skill.coins; i++) {
            boolean heads = player.getRandom().nextDouble() < headsChance;
            // 手持阿赖耶识时正负效果互换：反面算「好」
            boolean good = inverted != heads;
            if (good) {
                goodCount++;
            }
            faces.append(heads ? "正" : "反");
        }
        final double coinMultiplier = 0.2D + 1.8D * ((double) goodCount / Math.max(1, skill.coins));
        total *= (float) coinMultiplier;
        // 硬币结果通过 CoinMessage 同步给客户端，由 CoinHudOverlay 用图标显示（理智圆环下方）
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            int mask = 0;
            for (int i = 0; i < faces.length(); i++) {
                if (faces.charAt(i) == '正') {
                    mask |= 1 << i;
                }
            }
            net.minecraftforge.network.PacketDistributor.PacketTarget coinTarget =
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer);
            com.moonproject.moonmod.network.ModNetwork.CHANNEL.send(coinTarget,
                    new com.moonproject.moonmod.network.ModNetwork.CoinMessage(mask, faces.length(), coinMultiplier));
        }
        if (skill.heavy) {
            player.removeEffect(ModEffects.POISE.get());
            player.displayClientMessage(Component.translatable(
                    "item.moonproject.alaya_vijnana.skill.cast.name", skill.display, poise), true);
        } else {
            addStacks(player, ModEffects.POISE.get(),
                    MoonProjectConfig.poisePerFastSlash(), MoonProjectConfig.poiseMaxStacks());
        }
        // 原著：每个技能对「裂伤」的施加层数各不相同（挥砍4 / 涂抹3 / 泼洒5 / 描绘5 / 天杀4 / 抹去5）
        // 原著「天杀星刀-阿赖耶识」：技能增加的流血强度额外 +3 级
        int bleedBonus = Sanity.hasAbyssCall(player) ? 3 : 0;
        addStacks(target, ModEffects.BLEED.get(), skill.laceration + bleedBonus, MoonProjectConfig.bleedMaxStacks());

        for (int coin = 0; coin < skill.coins; coin++) {
            SlashScheduler.schedule(player, target, total / skill.coins, skill.heavy, coin * 2);
        }
    }

    /** 消耗斩缘。 */
    private static void takeCutbond(Player player, int amount) {
        int current = stackCount(player, ModEffects.CUTBOND.get());
        player.removeEffect(ModEffects.CUTBOND.get());
        int left = current - amount;
        if (left > 0) {
            player.addEffect(new MobEffectInstance(ModEffects.CUTBOND.get(),
                    MoonProjectConfig.stackDurationTicks(), left - 1, false, true));
        }
    }

    /** 横扫：正前方锥形内的其它敌对生物也吃 50% 伤害（同样延迟落地）。 */
    private static void scheduleSweep(Player player, LivingEntity primary, float damage, boolean slow) {
        double ratio = MoonProjectConfig.sweepDamageRatio();
        double radius = MoonProjectConfig.sweepRadius();
        if (ratio <= 0.0D || radius <= 0.0D) {
            return;
        }
        Vec3 look = player.getLookAngle();
        double cosLimit = Math.cos(Math.toRadians(MoonProjectConfig.sweepHalfAngleDegrees()));
        for (LivingEntity other : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius))) {
            if (other == primary || other == player || !other.isAlive()) {
                continue;
            }
            if (!(other instanceof Enemy) || player.isAlliedTo(other)) {
                continue;
            }
            Vec3 to = other.position().subtract(player.position()).normalize();
            if (look.dot(to) < cosLimit) {
                continue;
            }
            SlashScheduler.schedule(player, other, (float) (damage * ratio), slow);
        }
    }

    /** 由 {@link SlashScheduler} 调用：斩击真正落地，此时才结算状态。 */
    public static void applyDelayedHit(Player player, LivingEntity target, float damage, boolean slow) {
        applyingDelayed = true;
        try {
            target.invulnerableTime = 0; // 绕过 MC 无敌帧，否则多段硬币只有第一段生效
            target.hurt(player.damageSources().playerAttack(player), damage);

            // 无我梦中：每次挥出硬币获得斩缘
            addStacks(player, ModEffects.CUTBOND.get(), 1, MoonProjectConfig.cutbondMaxStacks());

            if (slow) {
                // 阿赖耶识专属（原著「裂伤 / 斩断」）：裂伤 + 斩断 + 焦黑斩痕
                applyScar(target);
                applyCutoff(target, player);
            } else {
                // 小指良那套：流血 + 烧伤
                // 遵循原著：左键系也施加「裂伤 / 斩断」（与 N/M 同一套，不再用通用版）
                applyCutoff(target, player);
            }
            // 状态显示交给准星 HUD（StatusHudOverlay），这里不再重复打到动作栏
            resolveCutoff(target);
        } finally {
            applyingDelayed = false;
        }
    }

    // ==================== 自动追击 ====================

    /** 突进：目标在 2.5 格以外、追击半径以内时，向目标突进一小段（有冷却），并施加斩断。 */
    private static void tryLunge(Player player, LivingEntity target, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getInt(AlayaVijnanaItem.TAG_LUNGE_COOLDOWN) > 0) {
            return;
        }
        double distance = player.distanceTo(target);
        if (distance < 2.5D || distance > MoonProjectConfig.autoChaseRadius()) {
            return;
        }
        Vec3 direction = new Vec3(target.getX() - player.getX(), 0.0D, target.getZ() - player.getZ()).normalize();
        double power = MoonProjectConfig.autoChasePower();
        player.setDeltaMovement(player.getDeltaMovement().add(direction.x * power, 0.18D, direction.z * power));
        player.hurtMarked = true;
        tag.putInt(AlayaVijnanaItem.TAG_LUNGE_COOLDOWN, MoonProjectConfig.autoChaseCooldownTicks());
        applyCutoff(target, player);
    }

    // ==================== 状态结算 ====================

    /** 命中触发型 DoT：按层数造成伤害，然后消耗 1 层（原著出血 / 裂伤的规则）。 */
    private static void resolveStackDot(LivingEntity target, MobEffect effect, int damagePerStack) {
        MobEffectInstance instance = target.getEffect(effect);
        if (instance == null) {
            return;
        }
        int stacks = instance.getAmplifier() + 1;
        int duration = instance.getDuration();
        target.removeEffect(effect);
        if (stacks > 1) {
            target.addEffect(new MobEffectInstance(effect, duration, stacks - 2, false, true));
        }
        target.hurt(target.damageSources().magic(), stacks * damagePerStack);
    }

    /** 支离灭裂：受到伤害时按（自身斩缘 + 对手斩断）减伤（原著 -90%，这里上限 -30%）。 */
    private static void applyMitigation(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        int cutbond = stackCount(target, ModEffects.CUTBOND.get());
        if (cutbond <= 0) {
            return;
        }
        int attackerCutoff = event.getSource().getEntity() instanceof LivingEntity attacker
                ? stackCount(attacker, ModEffects.CUTOFF.get())
                : 0;
        double reduction = Math.min(0.30D, (cutbond + attackerCutoff) * 0.005D);
        event.setAmount((float) (event.getAmount() * (1.0D - reduction)));
    }

    /** 斩断：施加 1 层，自身每 10 层斩缘额外 +1 层（最多 +4）。 */
    private static void applyCutoff(LivingEntity target, Player player) {
        int cutbond = stackCount(player, ModEffects.CUTBOND.get());
        int extra = Math.min(4, cutbond / 10);
        // 切丝效率提高：每次命中基础 5 层（阈值同步提高到 100，约 20 次命中触发爆发）
        addStacks(target, ModEffects.CUTOFF.get(), 5 + extra, MoonProjectConfig.cutoffMaxStacks());
    }

    /** 斩断叠满 → 触发「强力技能」：追加一次大伤害并清空层数。 */
    private static void resolveCutoff(LivingEntity target) {
        MobEffectInstance cutoff = target.getEffect(ModEffects.CUTOFF.get());
        if (cutoff == null) {
            return;
        }
        int stacks = cutoff.getAmplifier() + 1;
        if (stacks < MoonProjectConfig.cutoffTriggerStacks()) {
            return;
        }
        target.removeEffect(ModEffects.CUTOFF.get());
        target.hurt(target.damageSources().magic(),
                stacks * MoonProjectConfig.cutoffTriggerDamagePerStack());
    }

    /** 施加 / 叠加焦黑斩痕。 */
    /** 阿赖耶识命中后「禁疗」：一段时间内无法被治疗，死亡也不掉落。 */
    private static void applyScar(LivingEntity target) {
        int duration = MoonProjectConfig.scarDurationTicks();
        if (duration <= 0) {
            return;
        }
        target.getPersistentData().putLong(NO_HEAL_UNTIL, target.level().getGameTime() + duration);
    }

    /** 该生物当前是否处于禁疗状态。 */
    public static boolean isNoHeal(LivingEntity entity) {
        long until = entity.getPersistentData().getLong(NO_HEAL_UNTIL);
        return until > 0L && entity.level().getGameTime() < until;
    }

    public static int stackCount(LivingEntity entity, MobEffect effect) {
        MobEffectInstance instance = entity.getEffect(effect);
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    public static void addStacks(LivingEntity entity, MobEffect effect, int amount, int maxStacks) {
        if (amount <= 0 || maxStacks <= 0) {
            return;
        }
        int stacks = Math.min(stackCount(entity, effect) + amount, maxStacks);
        entity.addEffect(new MobEffectInstance(effect, MoonProjectConfig.stackDurationTicks(),
                stacks - 1, false, true));
    }

    /** 目标身上本模组状态的单行摘要（动作栏用）。 */
    public static Component describeStacks(LivingEntity target) {
        MutableComponent text = Component.empty();
        appendStack(text, target, ModEffects.BLEED.get());
        appendStack(text, target, ModEffects.BURN.get());
        appendStack(text, target, ModEffects.LACERATION.get());
        appendStack(text, target, ModEffects.RUPTURE.get());
        appendStack(text, target, ModEffects.TREMOR.get());
        appendStack(text, target, ModEffects.CUTOFF.get());
        appendStack(text, target, ModEffects.BLACK_SCAR.get());
        return text;
    }

    private static void appendStack(MutableComponent text, LivingEntity target, MobEffect effect) {
        int stacks = stackCount(target, effect);
        if (stacks <= 0) {
            return;
        }
        if (text.getString().length() > 0) {
            text.append(Component.literal(" "));
        }
        text.append(effect.getDisplayName()).append(Component.literal("x" + stacks));
    }

    // ==================== 技能：「将我抹去，也将你抹去。」 ====================

    /** 技能3 的层数总和：自身斩缘 + 目标的出血 / 火伤 / 裂伤。 */
    public static int totalStacks(LivingEntity target, Player attacker) {
        return stackCount(attacker, ModEffects.CUTBOND.get())
                + stackCount(target, ModEffects.BLEED.get())
                + stackCount(target, ModEffects.BURN.get())
                + stackCount(target, ModEffects.LACERATION.get());
    }

    /** 技能3（潜行 + 右键）：无视护甲的概念伤害，按层数总和放大，并施加裂伤与斩断。 */
    public static boolean castConceptSever(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getInt(AlayaVijnanaItem.TAG_SKILL_COOLDOWN) > 0) {
            player.displayClientMessage(Component.translatable(
                    "item.moonproject.alaya_vijnana.skill.cooldown",
                    tag.getInt(AlayaVijnanaItem.TAG_SKILL_COOLDOWN) / 20), true);
            return false;
        }
        LivingEntity target = findTarget(player, MoonProjectConfig.skill3Range());
        if (target == null) {
            player.displayClientMessage(
                    Component.translatable("item.moonproject.alaya_vijnana.skill.no_target"), true);
            return false;
        }
        // 抹去只通过专属键 V 释放；潜行 + 右键固定为描绘
        Skill skill = Skill.DEPICT;
        float base = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        scheduleSkill(player, target, skill, base);
        tag.putInt(AlayaVijnanaItem.TAG_SKILL_COOLDOWN, MoonProjectConfig.skill3CooldownTicks());
        return true;
    }

    /**
     * 专属技能键入口（服务端执行）：0 = 天杀，1 = 将我抹去，也将你抹去。
     * 由 {@code ModNetwork} 的按键网络包调用。
     */
    public static void castUltimateByKey(Player player, int skill) {
        ItemStack stack = player.getMainHandItem();
        if (!AlayaVijnanaItem.isDrawnBlade(stack)) {
            return;
        }
        boolean erase = skill == 1;
        int cost = erase ? MoonProjectConfig.eraseCutbondCost() : MoonProjectConfig.heavenlyKillCutbondCost();
        if (stackCount(player, ModEffects.CUTBOND.get()) < cost) {
            player.displayClientMessage(Component.translatable(
                    "item.moonproject.alaya_vijnana.skill.not_enough", cost), true);
            return;
        }
        LivingEntity target = findTarget(player, MoonProjectConfig.skill3Range());
        if (target == null) {
            player.displayClientMessage(
                    Component.translatable("item.moonproject.alaya_vijnana.skill.no_target"), true);
            return;
        }
        takeCutbond(player, cost);
        Skill chosen = erase ? Skill.ERASE : Skill.HEAVENLY_KILL;
        float base = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        scheduleSkill(player, target, chosen, base);
    }

    /** 在正前方约 60° 的锥形内，寻找最近的敌对生物。 */
    private static LivingEntity findTarget(Player player, double range) {
        Vec3 look = player.getLookAngle();
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range))) {
            if (candidate == player || !candidate.isAlive() || !(candidate instanceof Enemy)) {
                continue;
            }
            Vec3 to = candidate.position()
                    .add(0.0D, candidate.getBbHeight() * 0.5D, 0.0D)
                    .subtract(player.getEyePosition())
                    .normalize();
            if (look.dot(to) < 0.5D) {
                continue;
            }
            double distance = player.distanceTo(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private CombatEvents() {
    }
}
