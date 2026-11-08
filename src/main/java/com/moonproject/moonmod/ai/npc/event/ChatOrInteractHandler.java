package com.moonproject.moonmod.ai.npc.event;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.ai.npc.action.ActionDispatcher;
import com.moonproject.moonmod.ai.npc.config.AiNpcConfig;
import com.moonproject.moonmod.ai.npc.data.ChatRequest;
import com.moonproject.moonmod.ai.npc.network.AIServiceBridge;
import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import com.moonproject.moonmod.network.ModNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class ChatOrInteractHandler {
    private static final Logger LOGGER = LogManager.getLogger(ChatOrInteractHandler.class);

    // 记录玩家处于对话交互态的目标 NPC 及超时时间戳 (用作公屏聊天降级兼容)
    private static final Map<UUID, DialogSession> PENDING_SESSIONS = new ConcurrentHashMap<>();
    // 玩家请求冷却记录
    private static final Map<UUID, Long> PLAYER_COOLDOWNS = new ConcurrentHashMap<>();

    private static class DialogSession {
        final Entity npc;
        final long createdAt;

        DialogSession(Entity npc, long createdAt) {
            this.npc = npc;
            this.createdAt = createdAt;
        }
    }

    /**
     * 判断目标实体是否具备 AI NPC 交互资格
     */
    private static boolean isAiNpc(Entity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (AiNpcConfig.isAllowVillagers() && entity instanceof Villager) {
            return true;
        }
        String targetTag = AiNpcConfig.getNpcTag();
        if (targetTag != null && !targetTag.isEmpty() && entity.getTags().contains(targetTag)) {
            return true;
        }
        return entity.hasCustomName();
    }

    /**
     * 监听玩家右键点击实体事件：驻足停步、专注目视，弹出极简沉浸式半透明字幕输入条
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Entity target = event.getTarget();
        if (!isAiNpc(target)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 潜行右键（Shift+右键）：分支流打开女仆式随身行囊与管理界面 (Maid-style GUI)
        if (player.isShiftKeyDown()) {
            if (target instanceof AiPlayerNpcEntity aiNpc) {
                aiNpc.openInventory(player);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }

        long now = System.currentTimeMillis();
        long cooldownMs = AiNpcConfig.getCooldownSeconds() * 1000L;
        Long lastTime = PLAYER_COOLDOWNS.get(player.getUUID());
        if (lastTime != null && (now - lastTime) < cooldownMs) {
            long waitSec = (cooldownMs - (now - lastTime)) / 1000 + 1;
            player.sendSystemMessage(Component.literal("§c[AI NPC] 交流过于频繁，请等待 " + waitSec + " 秒..."));
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        PENDING_SESSIONS.put(player.getUUID(), new DialogSession(target, now));

        String npcName = target.getDisplayName().getString();
        int mood = 65;
        int affection = 20;
        String emotion = "happy";
        String greeting = "咕噜噜~ " + player.getScoreboardName() + "！找大肥鱼有什么事吗？";

        if (target instanceof AiPlayerNpcEntity playerNpc) {
            mood = playerNpc.getMood();
            affection = playerNpc.getAffection();
            emotion = playerNpc.getEmotion();
            // 核心修复：NPC 立即停止漫步并驻足面朝玩家，头顶浮空 3D 气泡，右臂热情招手！
            playerNpc.startConversing(player, 240);
            playerNpc.showSpeechBubble(greeting, 140);
            playerNpc.triggerAnimation(AiPlayerNpcEntity.ANIM_WAVE, 45);
        }

        // 下发数据包，开启极简底部半透明字幕条（100% 不遮挡视野）
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ModNetwork.OpenNpcDialogMessage(
                        target.getId(),
                        npcName,
                        mood,
                        affection,
                        emotion,
                        greeting
                )
        );

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /**
     * 监听玩家聊天输入事件 (作为未开 GUI 时的聊天栏降级通道)
     */
    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }

        DialogSession session = PENDING_SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - session.createdAt > 60000L || !session.npc.isAlive()) {
            PENDING_SESSIONS.remove(player.getUUID());
            return;
        }

        event.setCanceled(true);
        PENDING_SESSIONS.remove(player.getUUID());
        PLAYER_COOLDOWNS.put(player.getUUID(), now);

        String message = event.getRawText();
        Entity npc = session.npc;
        String npcName = npc.getDisplayName().getString();

        // 采集当前现场环境上下文
        ServerLevel level = player.serverLevel();
        String biome = level.getBiome(player.blockPosition())
                .unwrapKey()
                .map(k -> k.location().toString())
                .orElse("minecraft:plains");

        boolean isDay = level.isDay();
        String weather = level.isRaining() ? (level.isThundering() ? "thunder" : "rain") : "clear";

        ItemStack mainHandItem = player.getMainHandItem();
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(mainHandItem.getItem());
        String heldItem = (itemKey != null && !mainHandItem.isEmpty()) ? itemKey.toString() : "minecraft:air";

        ChatRequest.Environment env = new ChatRequest.Environment(biome, isDay, weather, heldItem);
        ChatRequest request = new ChatRequest(
                player.getStringUUID(),
                player.getScoreboardName(),
                npc.getStringUUID(),
                npcName,
                message,
                env
        );

        String serviceUrl = AiNpcConfig.getServiceUrl();
        int timeoutSec = AiNpcConfig.getTimeoutSeconds();

        AIServiceBridge.sendChatRequestAsync(request, serviceUrl, timeoutSec)
                .whenComplete((response, throwable) -> {
                    player.getServer().execute(() -> {
                        if (throwable != null) {
                            LOGGER.error("AI 对话交互处理失败: {}", throwable.getMessage());
                            player.sendSystemMessage(Component.literal("§c[AI NPC] " + npcName + " 似乎心不在焉，未能回应。（中间件离线或超时）"));
                        } else if (response != null) {
                            ActionDispatcher.executeActions(player, npc, response);
                        }
                    });
                });
    }

    /**
     * 服务端关闭时自动清理中间件子进程
     */
    @SubscribeEvent
    public static void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        com.moonproject.moonmod.ai.npc.network.ServiceProcessManager.stopService();
    }
}
