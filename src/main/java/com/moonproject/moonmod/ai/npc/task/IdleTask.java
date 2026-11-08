package com.moonproject.moonmod.ai.npc.task;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;

public class IdleTask implements INpcTask {
    @Override
    public String getTaskId() {
        return "idle";
    }

    @Override
    public String getDisplayName() {
        return "自由待命";
    }

    @Override
    public String getIcon() {
        return "🧘";
    }

    @Override
    public boolean canExecute(AiPlayerNpcEntity npc) {
        return true;
    }

    @Override
    public void onStart(AiPlayerNpcEntity npc) {
    }

    @Override
    public void onTick(AiPlayerNpcEntity npc) {
        // 每 40 ticks 自愈 1 点生命值
        if (npc.tickCount % 40 == 0 && npc.getHealth() < npc.getMaxHealth()) {
            npc.heal(1.0F);
        }
    }

    @Override
    public void onStop(AiPlayerNpcEntity npc) {
    }

    @Override
    public boolean isFinished(AiPlayerNpcEntity npc) {
        return false;
    }
}
