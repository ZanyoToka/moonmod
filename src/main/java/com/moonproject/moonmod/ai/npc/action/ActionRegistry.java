package com.moonproject.moonmod.ai.npc.action;

import com.google.gson.JsonObject;
import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 具身动作注册中心 (Action Registry)
 * 采用注册表模式统一管理所有 Bot 具身动作，极大简化未来复杂功能的扩展。
 */
public final class ActionRegistry {
    private static final Logger LOGGER = LogManager.getLogger(ActionRegistry.class);
    private static final Map<String, IActionHandler> HANDLERS = new ConcurrentHashMap<>();

    static {
        // 注册内置基础动作
        register(new GiveItemActionHandler());
        register(new PlaySoundActionHandler());
        register(new MoveToActionHandler());
        register(new FollowPlayerActionHandler());
        register(new EmoteActionHandler());
        register(new AnimationActionHandler());
    }

    private ActionRegistry() {}

    /**
     * 注册一个新的具身动作执行器
     */
    public static void register(IActionHandler handler) {
        if (handler != null && handler.getActionType() != null) {
            String key = handler.getActionType().trim().toUpperCase();
            HANDLERS.put(key, handler);
            LOGGER.debug("[AI NPC] 成功注册具身动作处理器: {}", key);
        }
    }

    /**
     * 执行指定动作
     */
    public static void dispatch(String actionType, ServerPlayer player, Entity npc, JsonObject params) {
        if (actionType == null || actionType.isBlank()) {
            return;
        }

        String key = actionType.trim().toUpperCase();
        IActionHandler handler = HANDLERS.get(key);

        if (handler != null) {
            try {
                handler.execute(player, npc, params != null ? params : new JsonObject());
            } catch (Exception e) {
                LOGGER.error("[AI NPC] 执行具身动作 [{}] 失败: {}", key, e.getMessage(), e);
            }
        } else {
            LOGGER.warn("[AI NPC] 未找到动作处理器: {} (已注册动作: {})", key, HANDLERS.keySet());
        }
    }

    // =========================================================================
    // 内置动作 1：发放物品 (GIVE_ITEM)
    // =========================================================================
    private static class GiveItemActionHandler implements IActionHandler {
        @Override
        public String getActionType() {
            return "GIVE_ITEM";
        }

        @Override
        public void execute(ServerPlayer player, Entity npc, JsonObject params) {
            String itemId = params.has("item") ? params.get("item").getAsString() : null;
            int count = params.has("count") ? params.get("count").getAsInt() : 1;

            if (itemId == null || itemId.isBlank()) return;

            ResourceLocation itemRl = ResourceLocation.tryParse(itemId);
            if (itemRl == null) return;

            if (!ForgeRegistries.ITEMS.containsKey(itemRl)) return;
            Item item = ForgeRegistries.ITEMS.getValue(itemRl);
            if (item == null || item == Items.AIR || item.getDescriptionId().contains("air")) return;

            int finalCount = Math.min(Math.max(1, count), item.getMaxStackSize());
            ItemStack stack = new ItemStack(item, finalCount);
            if (stack.isEmpty() || stack.is(Items.AIR)) return;

            // 如果有独立背包实体，同步扣减或记录
            if (npc instanceof AiPlayerNpcEntity aiNpc) {
                // NPC 具身送礼动作
                aiNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_HAPPY, 35);
            }

            boolean added = player.getInventory().add(stack);
            if (!added) {
                player.drop(stack, false);
                player.sendSystemMessage(Component.literal("§e[AI NPC] 背包已满，" + stack.getHoverName().getString() + " x" + finalCount + " 已掉落于脚边！"));
            } else {
                player.sendSystemMessage(Component.literal("§a[AI NPC] 赠予了你物品: " + stack.getHoverName().getString() + " x" + finalCount));
            }
        }
    }

    // =========================================================================
    // 内置动作 2：播放音效 (PLAY_SOUND)
    // =========================================================================
    private static class PlaySoundActionHandler implements IActionHandler {
        @Override
        public String getActionType() {
            return "PLAY_SOUND";
        }

        @Override
        public void execute(ServerPlayer player, Entity npc, JsonObject params) {
            String soundId = params.has("sound") ? params.get("sound").getAsString() : null;
            if (soundId == null || soundId.isBlank()) return;

            ResourceLocation soundRl = ResourceLocation.tryParse(soundId);
            if (soundRl == null) return;

            SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundRl);
            if (soundEvent == null) return;

            double x = (npc != null && npc.isAlive()) ? npc.getX() : player.getX();
            double y = (npc != null && npc.isAlive()) ? npc.getY() : player.getY();
            double z = (npc != null && npc.isAlive()) ? npc.getZ() : player.getZ();

            player.serverLevel().playSound(null, x, y, z, soundEvent, SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }

    // =========================================================================
    // 内置动作 3：原生物理寻路 (MOVE_TO)
    // =========================================================================
    private static class MoveToActionHandler implements IActionHandler {
        @Override
        public String getActionType() {
            return "MOVE_TO";
        }

        @Override
        public void execute(ServerPlayer player, Entity npc, JsonObject params) {
            if (!(npc instanceof Mob mob)) return;
            double x = params.has("x") ? params.get("x").getAsDouble() : player.getX();
            double y = params.has("y") ? params.get("y").getAsDouble() : player.getY();
            double z = params.has("z") ? params.get("z").getAsDouble() : player.getZ();
            double speed = params.has("speed") ? params.get("speed").getAsDouble() : 1.2D;

            mob.getNavigation().moveTo(x, y, z, speed);
        }
    }

    // =========================================================================
    // 内置动作 4：跟随玩家 (FOLLOW_PLAYER)
    // =========================================================================
    private static class FollowPlayerActionHandler implements IActionHandler {
        @Override
        public String getActionType() {
            return "FOLLOW_PLAYER";
        }

        @Override
        public void execute(ServerPlayer player, Entity npc, JsonObject params) {
            if (!(npc instanceof Mob mob)) return;
            mob.getNavigation().moveTo(player, 1.25D);
        }
    }

    // =========================================================================
    // 内置动作 5：头顶情绪粒子反馈 (EMOTE)
    // =========================================================================
    private static class EmoteActionHandler implements IActionHandler {
        @Override
        public String getActionType() {
            return "EMOTE";
        }

        @Override
        public void execute(ServerPlayer player, Entity npc, JsonObject params) {
            if (npc == null || !npc.isAlive()) return;
            ServerLevel level = player.serverLevel();
            String emotion = params.has("emotion") ? params.get("emotion").getAsString().toLowerCase() : "heart";

            double x = npc.getX();
            double y = npc.getY() + npc.getBbHeight() + 0.3D;
            double z = npc.getZ();

            switch (emotion) {
                case "angry" -> {
                    level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 5, 0.2, 0.2, 0.2, 0.0);
                    if (npc instanceof AiPlayerNpcEntity playerNpc) {
                        playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_ANGRY, 50);
                        playerNpc.setEmotion("angry");
                    }
                }
                case "happy", "touched" -> {
                    level.sendParticles(ParticleTypes.HEART, x, y, z, 4, 0.2, 0.2, 0.2, 0.0);
                    if (npc instanceof AiPlayerNpcEntity playerNpc) {
                        playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_HAPPY, 50);
                        playerNpc.setEmotion("happy");
                    }
                }
                case "excited" -> {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 8, 0.3, 0.3, 0.3, 0.0);
                    if (npc instanceof AiPlayerNpcEntity playerNpc) {
                        playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_WAVE, 60);
                        playerNpc.setEmotion("excited");
                    }
                }
                case "sad" -> {
                    level.sendParticles(ParticleTypes.SPLASH, x, y, z, 10, 0.2, 0.2, 0.2, 0.1);
                    if (npc instanceof AiPlayerNpcEntity playerNpc) {
                        playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_SAD, 60);
                        playerNpc.setEmotion("sad");
                    }
                }
                case "clap" -> {
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 5, 0.2, 0.2, 0.2, 0.0);
                    if (npc instanceof AiPlayerNpcEntity playerNpc) {
                        playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_CLAP, 50);
                        playerNpc.setEmotion("clap");
                    }
                }
                case "surprise" -> {
                    level.sendParticles(ParticleTypes.FIREWORK, x, y, z, 4, 0.1, 0.1, 0.1, 0.05);
                    if (npc instanceof AiPlayerNpcEntity playerNpc) {
                        playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_SURPRISE, 45);
                        playerNpc.setEmotion("surprise");
                    }
                }
                case "proud" -> {
                    level.sendParticles(ParticleTypes.ENCHANTED_HIT, x, y, z, 6, 0.2, 0.2, 0.2, 0.1);
                    if (npc instanceof AiPlayerNpcEntity playerNpc) {
                        playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_PROUD, 55);
                        playerNpc.setEmotion("proud");
                    }
                }
                case "dance" -> {
                    level.sendParticles(ParticleTypes.NOTE, x, y, z, 6, 0.3, 0.3, 0.3, 0.5);
                    if (npc instanceof AiPlayerNpcEntity playerNpc) {
                        playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_DANCE, 70);
                        playerNpc.setEmotion("dance");
                    }
                }
                default -> {
                    level.sendParticles(ParticleTypes.HEART, x, y, z, 2, 0.2, 0.2, 0.2, 0.0);
                }
            }
        }
    }

    // =========================================================================
    // 内置动作 6：3D 骨骼姿态动画派发 (ANIMATION)
    // =========================================================================
    private static class AnimationActionHandler implements IActionHandler {
        @Override
        public String getActionType() {
            return "ANIMATION";
        }

        @Override
        public void execute(ServerPlayer player, Entity npc, JsonObject params) {
            if (!(npc instanceof AiPlayerNpcEntity playerNpc)) return;
            String anim = params.has("anim") ? params.get("anim").getAsString().toLowerCase() : "talk";
            int duration = params.has("duration") ? params.get("duration").getAsInt() : 50;

            switch (anim) {
                case "wave" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_WAVE, duration);
                case "think" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_THINK, duration);
                case "happy" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_HAPPY, duration);
                case "bow" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_BOW, duration);
                case "talk" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_TALK, duration);
                case "angry" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_ANGRY, duration);
                case "sad" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_SAD, duration);
                case "clap" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_CLAP, duration);
                case "surprise" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_SURPRISE, duration);
                case "proud" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_PROUD, duration);
                case "dance" -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_DANCE, duration);
                default -> playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_IDLE, 0);
            }
        }
    }
}
