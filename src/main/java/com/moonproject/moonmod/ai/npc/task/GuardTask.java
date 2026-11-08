package com.moonproject.moonmod.ai.npc.task;

import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class GuardTask implements INpcTask {
    @Override
    public String getTaskId() {
        return "guard";
    }

    @Override
    public String getDisplayName() {
        return "战斗护卫";
    }

    @Override
    public String getIcon() {
        return "🛡️";
    }

    @Override
    public boolean canExecute(AiPlayerNpcEntity npc) {
        return true;
    }

    @Override
    public void onStart(AiPlayerNpcEntity npc) {
        npc.showSpeechBubble("进入警戒护卫状态！有危险我来抗！", 80);
        npc.triggerAnimation(AiPlayerNpcEntity.ANIM_PROUD, 40);
    }

    @Override
    public void onTick(AiPlayerNpcEntity npc) {
        Player player = npc.getConversingPlayer();
        if (player == null || !player.isAlive()) {
            player = npc.level().getNearestPlayer(npc, 16.0D);
        }

        // 寻找附近 10 格内的敌对怪物
        List<LivingEntity> enemies = npc.level().getEntitiesOfClass(LivingEntity.class, npc.getBoundingBox().inflate(10.0D),
                e -> e instanceof Enemy && e.isAlive() && !e.isAlliedTo(npc));

        if (!enemies.isEmpty()) {
            LivingEntity target = enemies.get(0);
            npc.getNavigation().moveTo(target, 1.35D);
            if (npc.distanceToSqr(target) <= 4.0D) {
                npc.swing(InteractionHand.MAIN_HAND);
                npc.doHurtTarget(target);
            }
        } else if (player != null) {
            // 没有敌人时，紧跟在玩家身边 4 格
            if (npc.distanceToSqr(player) > 16.0D) {
                npc.getNavigation().moveTo(player, 1.2D);
            } else {
                npc.getNavigation().stop();
            }
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
