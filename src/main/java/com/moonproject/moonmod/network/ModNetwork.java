package com.moonproject.moonmod.network;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.event.CombatEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * 专属技能键的网络通道：客户端按键 → 服务端执行技能。
 *
 * <p>只需一个消息：技能编号（0 = 天杀，1 = 将我抹去，也将你抹去。）。</p>
 */
public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MoonProjectMod.MODID, "main"))
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
    }

    /** 技能编号：0 = 天杀，1 = 将我抹去，也将你抹去。 */
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

    private ModNetwork() {
    }
}
