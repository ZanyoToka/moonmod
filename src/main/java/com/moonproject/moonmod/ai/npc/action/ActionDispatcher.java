package com.moonproject.moonmod.ai.npc.action;

import com.google.gson.JsonObject;
import com.moonproject.moonmod.ai.npc.data.ChatResponse;
import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ActionDispatcher {
    /**
     * 调度并执行 NPC 回传的台词与具身动作 (通过动作注册表总线派发)
     * 必须在主线程调度执行 (MinecraftServer.execute)
     */
    public static void executeActions(ServerPlayer player, Entity npc, ChatResponse response) {
        if (player == null || player.isRemoved() || response == null) {
            return;
        }

        String npcName = (npc != null && npc.isAlive()) ? npc.getDisplayName().getString() : "NPC";

        // 1. 发送 NPC 台词到玩家聊天栏 (供后台或查阅记录)
        if (!response.getSpeech().isEmpty()) {
            player.sendSystemMessage(Component.literal("§6[" + npcName + "§6] §f" + response.getSpeech()));
        }

        // 2. 如果是假人玩家，更新心情、好感度、情绪，展示头顶 3D 浮空漫画气泡，并专注面向玩家驻足
        if (npc instanceof AiPlayerNpcEntity playerNpc) {
            playerNpc.setMood(response.getMood());
            playerNpc.setAffection(response.getAffection());
            playerNpc.setEmotion(response.getEmotion());
            playerNpc.showSpeechBubble(response.getSpeech(), 140);
            playerNpc.startConversing(player, 220); // 驻足面向玩家 11 秒
            playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_TALK, 45);
        }

        // 3. 遍历下发的具身动作，交由 ActionRegistry 统一派发
        if (response.getActions() == null || response.getActions().isEmpty()) {
            return;
        }

        for (ChatResponse.Action action : response.getActions()) {
            if (action == null || action.getType() == null) {
                continue;
            }

            JsonObject params = new JsonObject();
            if (action.getItem() != null) params.addProperty("item", action.getItem());
            params.addProperty("count", action.getCount());
            if (action.getSound() != null) params.addProperty("sound", action.getSound());
            if (action.getEmotion() != null) params.addProperty("emotion", action.getEmotion());
            if (action.getAnim() != null) params.addProperty("anim", action.getAnim());
            params.addProperty("duration", action.getDuration());

            ActionRegistry.dispatch(action.getType(), player, npc, params);
        }
    }
}
