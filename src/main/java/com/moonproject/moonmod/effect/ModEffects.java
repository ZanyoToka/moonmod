package com.moonproject.moonmod.effect;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.config.MoonProjectConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 状态效果注册器——<b>严格按原著设定</b>，不再自创。
 *
 * <p>来源（2026-10-17 查证）：</p>
 * <ul>
 *   <li>《边狱巴士》「小指·蜘蛛巢之刃 良秀」：<b>出血 / 火伤 / 呼吸</b>三种；
 *       其技能强度由"呼吸与火伤/出血的层数总和"决定。</li>
 *   <li>《边狱巴士》第 9 章 9-49「友方-无我良秀」：<b>裂伤（Laceration）/ 斩断（Cutoff）/ 斩缘（Cutbond）</b>
 *       —— 裂伤是它对敌的 DoT，斩断是对敌的叠层，斩缘是自身的层数资源。</li>
 *   <li>「焦黑斩痕」为本模组阿赖耶识的自有能力，不属于上述体系。</li>
 * </ul>
 *
 * <p>MC 的单效果只有 `amplifier`（→ 层数）与 `duration` 两个数值，所以原著的
 * 「强度 + 层数」双数值在这里合并为<b>层数</b>：层数既决定每次触发伤害，也决定触发次数
 * （与原著的出血/裂伤"命中时按层数造成伤害并 -1 层"一致）。</p>
 */
public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MoonProjectMod.MODID);

    // ==================== 阿赖耶识自有 ====================

    /** 焦黑斩痕：被阿赖耶识斩中留下、无法愈合的焦痕（设计规格书 6.2）。 */
    public static final RegistryObject<MobEffect> BLACK_SCAR =
            EFFECTS.register("black_scar", BlackScarEffect::new);

    // ==================== 小指·蜘蛛巢之刃 良秀 的三种 ====================

    /** 出血：被击中时按层数受到伤害，层数 -1。 */
    public static final RegistryObject<MobEffect> BLEED =
            EFFECTS.register("bleed", () -> new StackEffect(MobEffectCategory.HARMFUL, 0x8B1A1A));

    /** 火伤：周期性火焰伤害（层数越高越痛）。 */
    public static final RegistryObject<MobEffect> BURN =
            EFFECTS.register("burn", () -> new PeriodicDamageEffect(MobEffectCategory.HARMFUL, 0xE07B1F,
                    MoonProjectConfig::burnDamage, MoonProjectConfig::burnIntervalTicks));

    /** 呼吸：自身增益，提升阿赖耶识造成的伤害；慢刀会一次燃尽。 */
    public static final RegistryObject<MobEffect> POISE =
            EFFECTS.register("poise", () -> new StackEffect(MobEffectCategory.BENEFICIAL, 0xD8E8F0));

    // ==================== 无我良秀 的三种 ====================

    /** 裂伤（Laceration）：无我良秀的主要 DoT——命中时按层数造成伤害，层数 -1。 */
    public static final RegistryObject<MobEffect> LACERATION =
            EFFECTS.register("laceration", () -> new StackEffect(MobEffectCategory.HARMFUL, 0xB03A5A));

    /** 斩断（Cutoff）：命中叠层；叠满后触发"强力技能"追加一次大伤害并清空。 */
    public static final RegistryObject<MobEffect> CUTOFF =
            EFFECTS.register("cutoff", () -> new StackEffect(MobEffectCategory.HARMFUL, 0x7A5CB0));

    /** 斩缘（Cutbond）：自身层数资源——提升伤害，并让斩断叠得更快。 */
    public static final RegistryObject<MobEffect> CUTBOND =
            EFFECTS.register("cutbond", () -> new StackEffect(MobEffectCategory.BENEFICIAL, 0xB0C8E8));

    private ModEffects() {
    }
}
