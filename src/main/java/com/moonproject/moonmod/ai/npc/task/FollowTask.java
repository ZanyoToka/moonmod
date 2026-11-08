package com.moonproject.moonmod.ai.npc.task;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import net.minecraft.world.entity.player.Player;

public class FollowTask implements INpcTask {
    @Override
    public String getTaskId() {
        return "follow";
    }

    @Override
    public String getDisplayName() {
        return "伴随跟随";
    }

    @Override
    public String getIcon() {
        return "🚶";
    }

    @Override
    public boolean canExecute(AiPlayerNpcEntity npc) {
        return true;
    }

    @Override
    public void onStart(AiPlayerNpcEntity npc) {
        npc.showSpeechBubble("好嘞，大肥鱼这就跟着你！", 80);
    }

    @Override
    public void onTick(AiPlayerNpcEntity npc) {
        Player player = npc.getConversingPlayer();
        if (player == null || !player.isAlive()) {
            player = npc.level().getNearestPlayer(npc, 16.0D);
        }
        if (player == null) return;

        double distSq = npc.distanceToSqr(player);
        if (distSq > 576.0D) { // > 24 blocks -> teleport
            npc.teleportTo(player.getX() + 1.0D, player.getY(), player.getZ() + 1.0D);
            npc.getNavigation().stop();
        } else if (distSq > 9.0D) { // > 3 blocks -> walk towards player
            npc.getNavigation().moveTo(player, 1.25D);
        } else {
            npc.getNavigation().stop();
        }
    }

    @Override
    public void onStop(AiPlayerNpcEntity npc) {
        npc.getNavigation().stop();
    }

    @Override
    public boolean isFinished(AiPlayerNpcEntity npc) {
        return false;
    }
}
