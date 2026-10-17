package com.moonproject.moonmod.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 模组数值集中管理（总纲第 7 节：所有平衡数值进 Config，禁止硬编码散落）。
 *
 * <p>注意：物品属性必须在「物品构造时」就确定，而 Config 的值要等加载后才可读，
 * 因此这里同时提供 DEFAULT_* 常量作为兜底；运行期统一通过下面的读取方法取值。</p>
 *
 * <p>数值来源：设计规格书 v0.3 + 用户追加要求（拔刀态 5 刀/秒、3 秒击杀监守者、攻击距离加长）。
 * 玩家侧可以直接改 run/config/moonproject-common.toml，不必改代码。</p>
 */
public final class MoonProjectConfig {
    public static final ForgeConfigSpec SPEC;

    // ===== 阿赖耶识：默认值（同时作为物品属性兜底） =====
    /** 鞘击态伤害修正：面板 = 玩家基础 1 + 本值 = 17（用户追加：整体再提高一档）。 */
    public static final double DEFAULT_SHEATHED_DAMAGE = 16.0D;
    /**
     * 解放态伤害修正：面板 = 1 + 本值 = 45。
     * 技能按原著硬币数分段（挥砍 2 段、天杀 5 段、抹去 5 段），总伤害 = 面板 × 技能倍率。
     */
    public static final double DEFAULT_DRAWN_DAMAGE = 44.0D;
    public static final double DEFAULT_SHEATHED_ATTACK_SPEED = -2.4D;
    /** 解放态攻速修正：每秒攻击次数 = 4 + 本值 = 5.0 次/秒。 */
    public static final double DEFAULT_DRAWN_ATTACK_SPEED = 1.0D;
    /** 攻击距离加成（格）：大太刀比普通剑长，两种形态都生效。 */
    public static final double DEFAULT_ENTITY_REACH_BONUS = 2.5D;
    public static final int DEFAULT_DRAW_DURATION_TICKS = 600;
    public static final int DEFAULT_SWITCH_COOLDOWN_TICKS = 60;

    public static final ForgeConfigSpec.DoubleValue ALAYA_SHEATHED_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ALAYA_DRAWN_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ALAYA_SHEATHED_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue ALAYA_DRAWN_ATTACK_SPEED;
    public static final ForgeConfigSpec.DoubleValue ALAYA_ENTITY_REACH;
    public static final ForgeConfigSpec.IntValue ALAYA_DRAW_DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue ALAYA_SWITCH_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue ALAYA_MEMORY_BURN_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue ALAYA_MEMORY_COST_PER_BURN;
    public static final ForgeConfigSpec.DoubleValue ALAYA_ARMOR_PIERCE;
    public static final ForgeConfigSpec.IntValue ALAYA_SCAR_DAMAGE;
    public static final ForgeConfigSpec.IntValue ALAYA_SCAR_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue ALAYA_SCAR_DURATION_TICKS;
    public static final ForgeConfigSpec.DoubleValue ALAYA_SCAR_REAPPLY_RADIUS;
    public static final ForgeConfigSpec.IntValue ALAYA_EROSION_PER_HIT;
    public static final ForgeConfigSpec.BooleanValue ALAYA_REQUIRES_BLOODLINE;

    // ===== 快慢刀 / 层数体系 / 自动追击 / 技能 =====
    /** 快刀伤害倍率（未蓄满时的单击）。 */
    public static final ForgeConfigSpec.DoubleValue ALAYA_FAST_SLASH_DAMAGE_RATIO;
    /** 慢刀每消耗 1 层呼吸附加的伤害比例。 */
    public static final ForgeConfigSpec.DoubleValue ALAYA_SLOW_SLASH_POISE_BONUS;
    public static final ForgeConfigSpec.IntValue ALAYA_POISE_PER_FAST_SLASH;
    public static final ForgeConfigSpec.IntValue ALAYA_POISE_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue ALAYA_BLEED_PER_FAST_SLASH;
    public static final ForgeConfigSpec.IntValue ALAYA_BLEED_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue ALAYA_SCAR_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue ALAYA_STACK_DURATION_TICKS;
    public static final ForgeConfigSpec.DoubleValue ALAYA_AUTO_CHASE_RADIUS;
    public static final ForgeConfigSpec.DoubleValue ALAYA_AUTO_CHASE_POWER;
    public static final ForgeConfigSpec.IntValue ALAYA_AUTO_CHASE_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue ALAYA_SWEEP_RADIUS;
    public static final ForgeConfigSpec.DoubleValue ALAYA_SWEEP_DAMAGE_RATIO;
    public static final ForgeConfigSpec.DoubleValue ALAYA_SWEEP_HALF_ANGLE;
    public static final ForgeConfigSpec.DoubleValue ALAYA_SKILL3_BASE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue ALAYA_SKILL3_STACK_BONUS;
    public static final ForgeConfigSpec.IntValue ALAYA_SKILL3_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.DoubleValue ALAYA_SKILL3_RANGE;

    // ===== 状态效果：DoT / Debuff / Buff（巴士 + 图书馆体系）=====
    public static final ForgeConfigSpec.IntValue BURN_DAMAGE;
    public static final ForgeConfigSpec.IntValue BURN_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue BURN_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue RUPTURE_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue TREMOR_THRESHOLD;
    public static final ForgeConfigSpec.IntValue TREMOR_DAMAGE_PER_STACK;
    public static final ForgeConfigSpec.IntValue WEAK_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue FRAIL_MAX_STACKS;
    public static final ForgeConfigSpec.DoubleValue FRAIL_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue WEAK_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue PROTECTION_MULTIPLIER;

    // ===== 原著作据的状态（小指良：出血/火伤/呼吸；无我良秀：裂伤/斩断/斩缘）=====
    public static final ForgeConfigSpec.IntValue LACERATION_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue CUTBOND_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue CUTOFF_MAX_STACKS;
    public static final ForgeConfigSpec.IntValue CUTOFF_TRIGGER_STACKS;
    public static final ForgeConfigSpec.IntValue CUTOFF_TRIGGER_DAMAGE_PER_STACK;

    // ===== 快慢刀：斩击延迟生效 =====
    public static final ForgeConfigSpec.IntValue FAST_SLASH_DELAY_TICKS;
    public static final ForgeConfigSpec.IntValue SLOW_SLASH_DELAY_TICKS;
    /** 慢刀的蓄力阈值（0~1）；「潜行 + 左键」无视阈值必定慢刀。 */
    public static final ForgeConfigSpec.DoubleValue SLOW_SLASH_CHARGE_THRESHOLD;
    /** 技能「天杀」（潜行 + 左键）的伤害倍率。 */
    public static final ForgeConfigSpec.DoubleValue HEAVY_SLASH_DAMAGE_MULTIPLIER;
    /** 大招「天杀」需要的斩缘层数（满足即消耗并升级）。 */
    public static final ForgeConfigSpec.IntValue HEAVENLY_KILL_CUTBOND_COST;
    /** 强化大招「将我抹去，也将你抹去。」需要的斩缘层数。 */
    public static final ForgeConfigSpec.IntValue ERASE_CUTBOND_COST;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("天杀星刀·阿赖耶识（详见 docs/《月亮计划模组-阿赖耶识设计规格.docx》）").push("alaya");

        ALAYA_SHEATHED_DAMAGE = builder
                .comment("鞘击态攻击伤害修正（游戏内面板 = 玩家基础 1 + 本值，默认 13 即面板 14）")
                .defineInRange("sheathedDamage", DEFAULT_SHEATHED_DAMAGE, 0.0D, 1000.0D);
        ALAYA_DRAWN_DAMAGE = builder
                .comment("解放态攻击伤害修正（默认 34 即面板 35；5 刀/秒下约 3 秒击杀监守者）")
                .defineInRange("drawnDamage", DEFAULT_DRAWN_DAMAGE, 0.0D, 1000.0D);
        ALAYA_SHEATHED_ATTACK_SPEED = builder
                .comment("鞘击态攻速修正（每秒攻击次数 = 4 + 本值，默认 -2.4 即 1.6 次/秒）")
                .defineInRange("sheathedAttackSpeed", DEFAULT_SHEATHED_ATTACK_SPEED, -4.0D, 8.0D);
        ALAYA_DRAWN_ATTACK_SPEED = builder
                .comment("解放态攻速修正（默认 1.0 即 5.0 次/秒；调到 0 就是 4.0 次/秒）")
                .defineInRange("drawnAttackSpeed", DEFAULT_DRAWN_ATTACK_SPEED, -4.0D, 8.0D);
        ALAYA_ENTITY_REACH = builder
                .comment("攻击距离加成（格，两种形态都生效；原版默认 3.0 格）")
                .defineInRange("entityReachBonus", DEFAULT_ENTITY_REACH_BONUS, 0.0D, 16.0D);
        ALAYA_DRAW_DURATION_TICKS = builder
                .comment("解放态持续时间（tick，默认 600 = 30 秒）")
                .defineInRange("drawDurationTicks", DEFAULT_DRAW_DURATION_TICKS, 20, 24000);
        ALAYA_SWITCH_COOLDOWN_TICKS = builder
                .comment("形态切换冷却（tick，默认 60 = 3 秒）")
                .defineInRange("switchCooldownTicks", DEFAULT_SWITCH_COOLDOWN_TICKS, 0, 24000);

        // ===== 步骤 2：记忆代价 / 焦黑斩痕 =====
        ALAYA_MEMORY_BURN_INTERVAL_TICKS = builder
                .comment("解放期间每隔多少 tick 烧掉一次使用者的记忆（默认 20 = 每秒 1 次）")
                .defineInRange("memoryBurnIntervalTicks", 20, 1, 1200);
        ALAYA_MEMORY_COST_PER_BURN = builder
                .comment("每次烧掉的记忆值（打样期以经验等级计；0 = 关闭记忆代价）")
                .defineInRange("memoryCostPerBurn", 1, 0, 100);
        ALAYA_ARMOR_PIERCE = builder
                .comment("解放态护甲穿透比例（0.25 = 无视 25% 护甲）")
                .defineInRange("armorPierce", 0.25D, 0.0D, 1.0D);
        ALAYA_SCAR_DAMAGE = builder
                .comment("焦黑斩痕每次结算的伤害（magic 伤害源，不吃护甲）")
                .defineInRange("scarDamage", 2, 0, 100);
        ALAYA_SCAR_INTERVAL_TICKS = builder
                .comment("焦黑斩痕结算间隔（tick，默认 40 = 每 2 秒一次）")
                .defineInRange("scarIntervalTicks", 40, 1, 1200);
        ALAYA_SCAR_DURATION_TICKS = builder
                .comment("焦黑斩痕持续时间（tick，默认 200 = 10 秒）")
                .defineInRange("scarDurationTicks", 200, 1, 12000);
        ALAYA_SCAR_REAPPLY_RADIUS = builder
                .comment("斩痕复发判定半径（格）：范围内有解放态的阿赖耶识就重新裂开")
                .defineInRange("scarReapplyRadius", 8.0D, 0.0D, 64.0D);
        ALAYA_EROSION_PER_HIT = builder
                .comment("解放态每次命中增加的侵蚀值（侵蚀系统上线后启用）")
                .defineInRange("erosionPerHit", 1, 0, 100);
        ALAYA_REQUIRES_BLOODLINE = builder
                .comment("是否启用适格者（时间感知血脉）门槛，血脉系统上线前保持 false")
                .define("requiresBloodline", false);

        // ===== 快慢刀 / 层数体系 / 自动追击 / 技能3 =====
        ALAYA_FAST_SLASH_DAMAGE_RATIO = builder
                .comment("快刀伤害倍率（攻击蓄力未满 90% 时；默认 0.55 即打 55% 伤害）")
                .defineInRange("fastSlashDamageRatio", 0.55D, 0.05D, 1.0D);
        ALAYA_SLOW_SLASH_POISE_BONUS = builder
                .comment("慢刀每燃尽 1 层呼吸附加的伤害比例（默认 0.08 即每层 +8%）")
                .defineInRange("slowSlashPoiseBonus", 0.08D, 0.0D, 1.0D);
        ALAYA_POISE_PER_FAST_SLASH = builder
                .comment("每次快刀给挥刀者叠加的呼吸层数")
                .defineInRange("poisePerFastSlash", 1, 0, 5);
        ALAYA_POISE_MAX_STACKS = builder
                .comment("呼吸层数上限")
                .defineInRange("poiseMaxStacks", 10, 1, 99);
        ALAYA_BLEED_PER_FAST_SLASH = builder
                .comment("每次快刀给目标叠加的出血层数")
                .defineInRange("bleedPerFastSlash", 1, 0, 5);
        ALAYA_BLEED_MAX_STACKS = builder
                .comment("出血层数上限")
                .defineInRange("bleedMaxStacks", 10, 1, 99);
        ALAYA_SCAR_MAX_STACKS = builder
                .comment("焦黑斩痕层数上限（层数越高，每次结算越痛）")
                .defineInRange("scarMaxStacks", 5, 1, 99);
        ALAYA_STACK_DURATION_TICKS = builder
                .comment("呼吸/出血层数的持续时间（tick，默认 300 = 15 秒）")
                .defineInRange("stackDurationTicks", 300, 20, 24000);
        ALAYA_AUTO_CHASE_RADIUS = builder
                .comment("自动追击（突进）的判定半径（格）")
                .defineInRange("autoChaseRadius", 8.0D, 0.0D, 32.0D);
        ALAYA_AUTO_CHASE_POWER = builder
                .comment("突进的水平初速度（默认 0.45）")
                .defineInRange("autoChasePower", 0.45D, 0.0D, 3.0D);
        ALAYA_AUTO_CHASE_COOLDOWN_TICKS = builder
                .comment("突进冷却（tick，默认 30 = 1.5 秒）")
                .defineInRange("autoChaseCooldownTicks", 30, 0, 1200);
        ALAYA_SWEEP_RADIUS = builder
                .comment("横扫（连带命中）的半径（格）")
                .defineInRange("sweepRadius", 3.5D, 0.0D, 16.0D);
        ALAYA_SWEEP_DAMAGE_RATIO = builder
                .comment("横扫对副目标的伤害倍率（0 = 关闭横扫）")
                .defineInRange("sweepDamageRatio", 0.5D, 0.0D, 2.0D);
        ALAYA_SWEEP_HALF_ANGLE = builder
                .comment("横扫锥形的半角（度）")
                .defineInRange("sweepHalfAngleDegrees", 60.0D, 5.0D, 180.0D);
        ALAYA_SKILL3_BASE_DAMAGE = builder
                .comment("技能3「概念斩断」的基础伤害（判定为无视护甲的概念伤害）")
                .defineInRange("skill3BaseDamage", 60.0D, 0.0D, 1000.0D);
        ALAYA_SKILL3_STACK_BONUS = builder
                .comment("技能3 按「呼吸 + 出血 + 斩痕」合计层数追加的伤害比例（默认 0.15 即每层 +15%）")
                .defineInRange("skill3StackBonus", 0.15D, 0.0D, 2.0D);
        ALAYA_SKILL3_COOLDOWN_TICKS = builder
                .comment("技能3 冷却（tick，默认 600 = 30 秒）")
                .defineInRange("skill3CooldownTicks", 600, 20, 24000);
        ALAYA_SKILL3_RANGE = builder
                .comment("技能3 的目标搜索距离（格）")
                .defineInRange("skill3Range", 8.0D, 1.0D, 32.0D);

        // ===== 状态效果（参考《废墟图书馆》/《边狱巴士》）=====
        BURN_DAMAGE = builder
                .comment("燃烧：每层每次结算的伤害")
                .defineInRange("burnDamage", 1, 0, 100);
        BURN_INTERVAL_TICKS = builder
                .comment("燃烧：结算间隔（tick，默认 40 = 每 2 秒）")
                .defineInRange("burnIntervalTicks", 40, 5, 1200);
        BURN_MAX_STACKS = builder
                .comment("烧伤层数上限（可叠加，越高每 2 秒掉得越多）")
                .defineInRange("burnMaxStacks", 20, 1, 99);
        RUPTURE_MAX_STACKS = builder
                .comment("破裂层数上限（命中时按层数爆发一次伤害并清空）")
                .defineInRange("ruptureMaxStacks", 10, 1, 99);
        TREMOR_THRESHOLD = builder
                .comment("震颤触发失衡所需的层数")
                .defineInRange("tremorThreshold", 5, 1, 99);
        TREMOR_DAMAGE_PER_STACK = builder
                .comment("震颤失衡时每层造成的伤害")
                .defineInRange("tremorDamagePerStack", 2, 0, 100);
        WEAK_MAX_STACKS = builder
                .comment("虚弱层数上限")
                .defineInRange("weakMaxStacks", 5, 1, 99);
        FRAIL_MAX_STACKS = builder
                .comment("脆弱层数上限")
                .defineInRange("frailMaxStacks", 5, 1, 99);
        FRAIL_MULTIPLIER = builder
                .comment("脆弱：受到伤害的倍率（默认 1.25 = 提高 25%）")
                .defineInRange("frailMultiplier", 1.25D, 1.0D, 5.0D);
        WEAK_MULTIPLIER = builder
                .comment("虚弱：造成伤害的倍率（默认 0.75 = 降低 25%）")
                .defineInRange("weakMultiplier", 0.75D, 0.0D, 1.0D);
        PROTECTION_MULTIPLIER = builder
                .comment("加护：受到伤害的倍率（默认 0.8 = 降低 20%）")
                .defineInRange("protectionMultiplier", 0.8D, 0.0D, 1.0D);

        // ===== 原著作据的状态（旧版的 rupture/tremor/weak/frail 键已停用，保留不读）=====
        LACERATION_MAX_STACKS = builder
                .comment("破裂层数上限（无我良秀的 DoT，比出血更重：每层 2 点伤害；可叠加）")
                .defineInRange("lacerationMaxStacks", 20, 1, 99);
        CUTBOND_MAX_STACKS = builder
                .comment("斩缘层数上限（自身资源：每次命中 +1，提升减伤与震颤叠层）")
                .defineInRange("cutbondMaxStacks", 100, 1, 999);
        CUTOFF_MAX_STACKS = builder
                .comment("斩断层数上限")
                .defineInRange("cutoffMaxStacks", 30, 1, 999);
        CUTOFF_TRIGGER_STACKS = builder
                .comment("斩断叠到多少层触发「强力技能」（原著 100 层，这里按 MC 节奏缩小）")
                .defineInRange("cutoffTriggerStacks", 20, 1, 999);
        CUTOFF_TRIGGER_DAMAGE_PER_STACK = builder
                .comment("强力技能每层斩断造成的伤害")
                .defineInRange("cutoffTriggerDamagePerStack", 3, 0, 100);

        // ===== 快慢刀：斩击延迟生效（原著的手感）=====
        FAST_SLASH_DELAY_TICKS = builder
                .comment("快刀落地延迟（tick，默认 4 = 0.2 秒）")
                .defineInRange("fastSlashDelayTicks", 4, 0, 100);
        SLOW_SLASH_DELAY_TICKS = builder
                .comment("慢刀落地延迟（tick，默认 12 = 0.6 秒；期间被打中会打断）")
                .defineInRange("slowSlashDelayTicks", 12, 0, 100);
        SLOW_SLASH_CHARGE_THRESHOLD = builder
                .comment("慢刀的蓄力阈值（默认 0.5 = 蓄力过半即算慢刀；潜行 + 左键无视此值必定慢刀）")
                .defineInRange("slowSlashChargeThreshold", 0.5D, 0.0D, 1.0D);
        HEAVY_SLASH_DAMAGE_MULTIPLIER = builder
                .comment("技能「天杀」（潜行 + 左键）的伤害倍率（默认 2.0 = 双倍）")
                .defineInRange("heavySlashDamageMultiplier", 2.0D, 1.0D, 10.0D);
        HEAVENLY_KILL_CUTBOND_COST = builder
                .comment("大招「天杀」需要的斩缘层数（满足时消耗并升级为天杀，否则放泼洒）")
                .defineInRange("heavenlyKillCutbondCost", 20, 1, 999);
        ERASE_CUTBOND_COST = builder
                .comment("强化大招「将我抹去，也将你抹去。」需要的斩缘层数（否则放描绘）")
                .defineInRange("eraseCutbondCost", 40, 1, 999);

        builder.pop();
        SPEC = builder.build();
    }

    private MoonProjectConfig() {
    }

    // ===== 读取入口：Config 未加载时回落到默认值，避免注册期崩溃 =====
    public static double sheathedDamage() {
        return SPEC.isLoaded() ? ALAYA_SHEATHED_DAMAGE.get() : DEFAULT_SHEATHED_DAMAGE;
    }

    public static double drawnDamage() {
        return SPEC.isLoaded() ? ALAYA_DRAWN_DAMAGE.get() : DEFAULT_DRAWN_DAMAGE;
    }

    public static double sheathedAttackSpeed() {
        return SPEC.isLoaded() ? ALAYA_SHEATHED_ATTACK_SPEED.get() : DEFAULT_SHEATHED_ATTACK_SPEED;
    }

    public static double drawnAttackSpeed() {
        return SPEC.isLoaded() ? ALAYA_DRAWN_ATTACK_SPEED.get() : DEFAULT_DRAWN_ATTACK_SPEED;
    }

    public static double entityReachBonus() {
        return SPEC.isLoaded() ? ALAYA_ENTITY_REACH.get() : DEFAULT_ENTITY_REACH_BONUS;
    }

    public static int drawDurationTicks() {
        return SPEC.isLoaded() ? ALAYA_DRAW_DURATION_TICKS.get() : DEFAULT_DRAW_DURATION_TICKS;
    }

    public static int switchCooldownTicks() {
        return SPEC.isLoaded() ? ALAYA_SWITCH_COOLDOWN_TICKS.get() : DEFAULT_SWITCH_COOLDOWN_TICKS;
    }

    /** 解放期间每隔多少 tick 烧一次记忆。 */
    public static int memoryBurnIntervalTicks() {
        return SPEC.isLoaded() ? ALAYA_MEMORY_BURN_INTERVAL_TICKS.get() : 20;
    }

    /** 每次烧掉的记忆（经验等级）；0 = 关闭记忆代价。 */
    public static int memoryCostPerBurn() {
        return SPEC.isLoaded() ? ALAYA_MEMORY_COST_PER_BURN.get() : 1;
    }

    public static int scarDamage() {
        return SPEC.isLoaded() ? ALAYA_SCAR_DAMAGE.get() : 2;
    }

    public static int scarIntervalTicks() {
        return SPEC.isLoaded() ? ALAYA_SCAR_INTERVAL_TICKS.get() : 40;
    }

    public static int scarDurationTicks() {
        return SPEC.isLoaded() ? ALAYA_SCAR_DURATION_TICKS.get() : 200;
    }

    public static double scarReapplyRadius() {
        return SPEC.isLoaded() ? ALAYA_SCAR_REAPPLY_RADIUS.get() : 8.0D;
    }

    // ===== 快慢刀 / 层数体系 =====
    public static double fastSlashDamageRatio() {
        return SPEC.isLoaded() ? ALAYA_FAST_SLASH_DAMAGE_RATIO.get() : 0.55D;
    }

    public static double slowSlashPoiseBonus() {
        return SPEC.isLoaded() ? ALAYA_SLOW_SLASH_POISE_BONUS.get() : 0.08D;
    }

    public static int poisePerFastSlash() {
        return SPEC.isLoaded() ? ALAYA_POISE_PER_FAST_SLASH.get() : 1;
    }

    public static int poiseMaxStacks() {
        return SPEC.isLoaded() ? ALAYA_POISE_MAX_STACKS.get() : 10;
    }

    public static int bleedPerFastSlash() {
        return SPEC.isLoaded() ? ALAYA_BLEED_PER_FAST_SLASH.get() : 1;
    }

    public static int bleedMaxStacks() {
        return SPEC.isLoaded() ? ALAYA_BLEED_MAX_STACKS.get() : 10;
    }

    public static int scarMaxStacks() {
        return SPEC.isLoaded() ? ALAYA_SCAR_MAX_STACKS.get() : 5;
    }

    public static int stackDurationTicks() {
        return SPEC.isLoaded() ? ALAYA_STACK_DURATION_TICKS.get() : 300;
    }

    // ===== 自动追击 =====
    public static double autoChaseRadius() {
        return SPEC.isLoaded() ? ALAYA_AUTO_CHASE_RADIUS.get() : 8.0D;
    }

    public static double autoChasePower() {
        return SPEC.isLoaded() ? ALAYA_AUTO_CHASE_POWER.get() : 0.45D;
    }

    public static int autoChaseCooldownTicks() {
        return SPEC.isLoaded() ? ALAYA_AUTO_CHASE_COOLDOWN_TICKS.get() : 30;
    }

    public static double sweepRadius() {
        return SPEC.isLoaded() ? ALAYA_SWEEP_RADIUS.get() : 3.5D;
    }

    public static double sweepDamageRatio() {
        return SPEC.isLoaded() ? ALAYA_SWEEP_DAMAGE_RATIO.get() : 0.5D;
    }

    public static double sweepHalfAngleDegrees() {
        return SPEC.isLoaded() ? ALAYA_SWEEP_HALF_ANGLE.get() : 60.0D;
    }

    // ===== 技能3：概念斩断 =====
    public static double skill3BaseDamage() {
        return SPEC.isLoaded() ? ALAYA_SKILL3_BASE_DAMAGE.get() : 60.0D;
    }

    public static double skill3StackBonus() {
        return SPEC.isLoaded() ? ALAYA_SKILL3_STACK_BONUS.get() : 0.15D;
    }

    public static int skill3CooldownTicks() {
        return SPEC.isLoaded() ? ALAYA_SKILL3_COOLDOWN_TICKS.get() : 600;
    }

    public static double skill3Range() {
        return SPEC.isLoaded() ? ALAYA_SKILL3_RANGE.get() : 8.0D;
    }

    // ===== 状态效果 =====
    public static int burnDamage() {
        return SPEC.isLoaded() ? BURN_DAMAGE.get() : 1;
    }

    public static int burnIntervalTicks() {
        return SPEC.isLoaded() ? BURN_INTERVAL_TICKS.get() : 40;
    }

    public static int burnMaxStacks() {
        return SPEC.isLoaded() ? BURN_MAX_STACKS.get() : 10;
    }

    public static int ruptureMaxStacks() {
        return SPEC.isLoaded() ? RUPTURE_MAX_STACKS.get() : 10;
    }

    public static int tremorThreshold() {
        return SPEC.isLoaded() ? TREMOR_THRESHOLD.get() : 5;
    }

    public static int tremorDamagePerStack() {
        return SPEC.isLoaded() ? TREMOR_DAMAGE_PER_STACK.get() : 2;
    }

    public static int weakMaxStacks() {
        return SPEC.isLoaded() ? WEAK_MAX_STACKS.get() : 5;
    }

    public static int frailMaxStacks() {
        return SPEC.isLoaded() ? FRAIL_MAX_STACKS.get() : 5;
    }

    public static double frailMultiplier() {
        return SPEC.isLoaded() ? FRAIL_MULTIPLIER.get() : 1.25D;
    }

    public static double weakMultiplier() {
        return SPEC.isLoaded() ? WEAK_MULTIPLIER.get() : 0.75D;
    }

    public static double protectionMultiplier() {
        return SPEC.isLoaded() ? PROTECTION_MULTIPLIER.get() : 0.8D;
    }

    // ===== 原著作据的状态 =====
    public static int lacerationMaxStacks() {
        return SPEC.isLoaded() ? LACERATION_MAX_STACKS.get() : 10;
    }

    public static int cutbondMaxStacks() {
        return SPEC.isLoaded() ? CUTBOND_MAX_STACKS.get() : 40;
    }

    public static int cutoffMaxStacks() {
        return SPEC.isLoaded() ? CUTOFF_MAX_STACKS.get() : 200;
    }

    public static int cutoffTriggerStacks() {
        return SPEC.isLoaded() ? CUTOFF_TRIGGER_STACKS.get() : 100;
    }

    public static int cutoffTriggerDamagePerStack() {
        return SPEC.isLoaded() ? CUTOFF_TRIGGER_DAMAGE_PER_STACK.get() : 3;
    }

    // ===== 快慢刀：斩击延迟生效 =====
    public static int fastSlashDelayTicks() {
        return SPEC.isLoaded() ? FAST_SLASH_DELAY_TICKS.get() : 4;
    }

    public static int slowSlashDelayTicks() {
        return SPEC.isLoaded() ? SLOW_SLASH_DELAY_TICKS.get() : 12;
    }

    public static double slowSlashChargeThreshold() {
        return SPEC.isLoaded() ? SLOW_SLASH_CHARGE_THRESHOLD.get() : 0.5D;
    }

    public static double heavySlashDamageMultiplier() {
        return SPEC.isLoaded() ? HEAVY_SLASH_DAMAGE_MULTIPLIER.get() : 2.0D;
    }

    public static int heavenlyKillCutbondCost() {
        return SPEC.isLoaded() ? HEAVENLY_KILL_CUTBOND_COST.get() : 20;
    }

    public static int eraseCutbondCost() {
        return SPEC.isLoaded() ? ERASE_CUTBOND_COST.get() : 40;
    }
}
