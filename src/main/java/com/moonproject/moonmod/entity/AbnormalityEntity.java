package com.moonproject.moonmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/**
 * 所有异想体实体的抽象基类（规划 5.3：10 种异想体 Boss 共用骨架）。
 * 子类职责：
 * <ul>
 *   <li>在 {@code registerGoals()} 里装配行为：远程弹幕 / 自爆 / 召唤衍生物；</li>
 *   <li>多阶段 Boss（天启鸟 / 白夜 / 一无所有）在 {@code tick()} 里按血量切换阶段；</li>
 *   <li>精神伤害：攻击命中时调用 Sanity 系统扣 SP（规划机制 4）；</li>
 *   <li>掉落：对应专属核心（data/moonproject/loot_tables/）。</li>
 * </ul>
 */
public abstract class AbnormalityEntity extends PathfinderMob {
    protected AbnormalityEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }
}
