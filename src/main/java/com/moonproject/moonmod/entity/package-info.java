/**
 * 生物包（规划第 4 章 entity/）。建议子包：
 * <ul>
 *   <li>{@code boss/} —— ALEPH 级：一无所有（三形态）、白夜（二阶段）、天启鸟（三阶段）；</li>
 *   <li>{@code mob/} —— 衍生物（雪之奴仆、尸山残块、死蝶、白夜信徒）与收容失效暴走体；</li>
 *   <li>{@code npc/} —— 司书、公司员工、流浪收尾人（交易/任务，可用村民式 AI 起步）；</li>
 *   <li>{@code projectile/} —— 弹幕实体（黑白弹幕、蓝焰、光柱；魔弹式弹射参考 {@code AbstractArrow}）。</li>
 * </ul>
 * 所有异想体继承 {@link com.moonproject.moonmod.entity.AbnormalityEntity}，
 * 注册统一走 {@code init/ModEntities}，动画用 GeckoLib（assets/moonproject/animmodels/）。
 */
package com.moonproject.moonmod.entity;
