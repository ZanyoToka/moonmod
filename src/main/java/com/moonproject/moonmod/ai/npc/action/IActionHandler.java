package com.moonproject.moonmod.ai.npc.action;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * 可插拔具身动作执行器接口。
 * 任何未来复杂行为（如自动寻路、挖矿、战斗、GeckoLib 3D 骨骼动画等），
 * 只需实现此接口并通过 ActionRegistry.register() 注册即可，无需修改模组核心逻辑。
 */
public interface IActionHandler {
    /**
     * 动作唯一标识字符串（如 GIVE_ITEM, MOVE_TO, FOLLOW_PLAYER, ATTACK_TARGET）
     */
    String getActionType();

    /**
     * 在服务端主 Tick 线程安全执行具体的具身操作
     *
     * @param player 与 NPC 互动的玩家
     * @param npc    执行动作的 NPC / Bot 实体
     * @param params 动作附带的参数 JSON 对象（由中间件或大模型下发）
     */
    void execute(ServerPlayer player, Entity npc, JsonObject params);
}
