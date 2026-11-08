package com.moonproject.moonmod.network;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.ai.npc.action.ActionDispatcher;
import com.moonproject.moonmod.ai.npc.config.AiNpcConfig;
import com.moonproject.moonmod.ai.npc.data.ChatRequest;
import com.moonproject.moonmod.ai.npc.network.AIServiceBridge;
import com.moonproject.moonmod.client.gui.NpcDialogScreen;
import com.moonproject.moonmod.entity.AiPlayerNpcEntity;
import com.moonproject.moonmod.event.CombatEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * 模组主网络通道：负责技能按键、理智硬币同步，以及沉浸式 AI 对话 GUI 的双向网络包。
 */
public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(MoonProjectMod.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    /** 注册消息（在 mod 总线的 FMLCommonSetupEvent 里调用一次）。 */
    @Mod.EventBusSubscriber(modid = MoonProjectMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        @SubscribeEvent
        public static void onCommonSetup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
            event.enqueueWork(ModNetwork::register);
        }
    }

    /** 注册消息（在 mod 总线初始化时调用一次）。 */
    public static void register() {
        CHANNEL.messageBuilder(CastSkillMessage.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CastSkillMessage::encode)
                .decoder(CastSkillMessage::decode)
                .consumerMainThread(CastSkillMessage::handle)
                .add();

        CHANNEL.messageBuilder(SanityMessage.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SanityMessage::encode)
                .decoder(SanityMessage::decode)
                .consumerMainThread(SanityMessage::handle)
                .add();

        CHANNEL.messageBuilder(CoinMessage.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CoinMessage::encode)
                .decoder(CoinMessage::decode)
                .consumerMainThread(CoinMessage::handle)
                .add();

        // 3. 打开 NPC 对话 GUI (服务端 -> 客户端)
        CHANNEL.messageBuilder(OpenNpcDialogMessage.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenNpcDialogMessage::encode)
                .decoder(OpenNpcDialogMessage::decode)
                .consumerMainThread(OpenNpcDialogMessage::handle)
                .add();

        // 4. 发送 NPC 对话 (客户端 -> 服务端)
        CHANNEL.messageBuilder(SendNpcChatMessage.class, 4, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SendNpcChatMessage::encode)
                .decoder(SendNpcChatMessage::decode)
                .consumerMainThread(SendNpcChatMessage::handle)
                .add();

        // 5. NPC 对话回复下发 (服务端 -> 客户端)
        CHANNEL.messageBuilder(NpcDialogReplyMessage.class, 5, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NpcDialogReplyMessage::encode)
                .decoder(NpcDialogReplyMessage::decode)
                .consumerMainThread(NpcDialogReplyMessage::handle)
                .add();

        // 6. 切换 NPC 行为任务 (客户端 -> 服务端)
        CHANNEL.messageBuilder(SwitchNpcTaskMessage.class, 6, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SwitchNpcTaskMessage::encode)
                .decoder(SwitchNpcTaskMessage::decode)
                .consumerMainThread(SwitchNpcTaskMessage::handle)
                .add();
    }

    // =========================================================================
    // 基础技能与属性消息
    // =========================================================================
    public record CastSkillMessage(int skill) {
        public static void encode(CastSkillMessage message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.skill());
        }

        public static CastSkillMessage decode(FriendlyByteBuf buffer) {
            return new CastSkillMessage(buffer.readVarInt());
        }

        public static void handle(CastSkillMessage message, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player != null) {
                    CombatEvents.castUltimateByKey(player, message.skill());
                }
            });
            ctx.setPacketHandled(true);
        }
    }

    public record SanityMessage(int sanity) {
        public static void encode(SanityMessage message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.sanity());
        }

        public static SanityMessage decode(FriendlyByteBuf buffer) {
            return new SanityMessage(buffer.readVarInt());
        }

        public static void handle(SanityMessage message, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> com.moonproject.moonmod.client.SanityClient.set(message.sanity())));
            ctx.setPacketHandled(true);
        }
    }

    public record CoinMessage(int mask, int count, double multiplier) {
        public static void encode(CoinMessage message, FriendlyByteBuf buffer) {
            buffer.writeVarInt(message.mask());
            buffer.writeVarInt(message.count());
            buffer.writeDouble(message.multiplier());
        }

        public static CoinMessage decode(FriendlyByteBuf buffer) {
            return new CoinMessage(buffer.readVarInt(), buffer.readVarInt(), buffer.readDouble());
        }

        public static void handle(CoinMessage message, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> com.moonproject.moonmod.client.CoinClient.set(
                            message.mask(), message.count(), message.multiplier())));
            ctx.setPacketHandled(true);
        }
    }

    // =========================================================================
    // AI NPC 沉浸式对话消息
    // =========================================================================
    public record OpenNpcDialogMessage(int entityId, String npcName, int mood, int affection, String emotion, String greeting) {
        public static void encode(OpenNpcDialogMessage msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entityId());
            buf.writeUtf(msg.npcName());
            buf.writeVarInt(msg.mood());
            buf.writeVarInt(msg.affection());
            buf.writeUtf(msg.emotion());
            buf.writeUtf(msg.greeting());
        }

        public static OpenNpcDialogMessage decode(FriendlyByteBuf buf) {
            return new OpenNpcDialogMessage(
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readUtf()
            );
        }

        public static void handle(OpenNpcDialogMessage msg, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(new NpcDialogScreen(
                        msg.entityId(), msg.npcName(), msg.mood(), msg.affection(), msg.emotion(), msg.greeting()
                ));
            }));
            ctx.setPacketHandled(true);
        }
    }

    public record SendNpcChatMessage(int entityId, String message) {
        public static void encode(SendNpcChatMessage msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entityId());
            buf.writeUtf(msg.message());
        }

        public static SendNpcChatMessage decode(FriendlyByteBuf buf) {
            return new SendNpcChatMessage(buf.readVarInt(), buf.readUtf());
        }

        public static void handle(SendNpcChatMessage msg, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player == null) return;

                ServerLevel level = player.serverLevel();
                Entity target = level.getEntity(msg.entityId());
                if (target == null || !target.isAlive()) return;

                String npcName = target.getDisplayName().getString();
                if (target instanceof AiPlayerNpcEntity playerNpc) {
                    playerNpc.startConversing(player, 240);
                }

                // 采集现场环境
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
                        target.getStringUUID(),
                        npcName,
                        msg.message(),
                        env
                );

                String serviceUrl = AiNpcConfig.getServiceUrl();
                int timeoutSec = AiNpcConfig.getTimeoutSeconds();

                AIServiceBridge.sendChatRequestAsync(request, serviceUrl, timeoutSec)
                        .whenComplete((response, throwable) -> {
                            player.getServer().execute(() -> {
                                if (throwable != null || response == null) {
                                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                                            new NpcDialogReplyMessage(msg.entityId(), "咕噜噜...（水波有点大，本鱼好像走神了）", 60, 20, "neutral"));
                                } else {
                                    // 派发具身动作
                                    ActionDispatcher.executeActions(player, target, response);
                                    // 同步更新 GUI 对话框
                                    CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                                            new NpcDialogReplyMessage(msg.entityId(), response.getSpeech(), response.getMood(), response.getAffection(), response.getEmotion()));
                                }
                            });
                        });
            });
            ctx.setPacketHandled(true);
        }
    }

    public record NpcDialogReplyMessage(int entityId, String speech, int mood, int affection, String emotion) {
        public static void encode(NpcDialogReplyMessage msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.entityId());
            buf.writeUtf(msg.speech());
            buf.writeVarInt(msg.mood());
            buf.writeVarInt(msg.affection());
            buf.writeUtf(msg.emotion());
        }

        public static NpcDialogReplyMessage decode(FriendlyByteBuf buf) {
            return new NpcDialogReplyMessage(
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUtf()
            );
        }

        public static void handle(NpcDialogReplyMessage msg, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (NpcDialogScreen.activeInstance != null) {
                    NpcDialogScreen.activeInstance.onReplyReceived(msg.speech(), msg.mood(), msg.affection(), msg.emotion());
                }
            }));
            ctx.setPacketHandled(true);
        }
    }

    // =========================================================================
    // 6. 切换 NPC 任务模式消息 (Maid-style Task Switch)
    // =========================================================================
    public record SwitchNpcTaskMessage(int npcEntityId, String taskId) {
        public static void encode(SwitchNpcTaskMessage msg, FriendlyByteBuf buf) {
            buf.writeVarInt(msg.npcEntityId());
            buf.writeUtf(msg.taskId());
        }

        public static SwitchNpcTaskMessage decode(FriendlyByteBuf buf) {
            return new SwitchNpcTaskMessage(buf.readVarInt(), buf.readUtf());
        }

        public static void handle(SwitchNpcTaskMessage msg, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            ctx.enqueueWork(() -> {
                ServerPlayer player = ctx.getSender();
                if (player != null && player.level() instanceof ServerLevel serverLevel) {
                    Entity entity = serverLevel.getEntity(msg.npcEntityId());
                    if (entity instanceof AiPlayerNpcEntity npc && entity.distanceToSqr(player) <= 64.0D) {
                        npc.getTaskManager().switchTask(msg.taskId());
                    }
                }
            });
            ctx.setPacketHandled(true);
        }
    }

    private ModNetwork() {
    }
}
