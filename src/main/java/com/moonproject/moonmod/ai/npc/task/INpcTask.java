package com.moonproject.moonmod.ai.npc.task;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;

/**
 * 模块化 NPC 行为任务接口 (参考 Touhou Little Maid 的 IMaidTask 架构设计)
 * 每个 Task 封装了独立的执行准入条件、生命周期以及现场业务逻辑。
 */
public interface INpcTask {
    String getTaskId();
    String getDisplayName();
    String getIcon();

    boolean canExecute(AiPlayerNpcEntity npc);
    void onStart(AiPlayerNpcEntity npc);
    void onTick(AiPlayerNpcEntity npc);
    void onStop(AiPlayerNpcEntity npc);
    boolean isFinished(AiPlayerNpcEntity npc);
}
