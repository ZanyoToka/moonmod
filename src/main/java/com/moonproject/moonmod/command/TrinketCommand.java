package com.moonproject.moonmod.command;

import com.moonproject.moonmod.MoonProjectMod;
import com.moonproject.moonmod.trinket.TrinketHelper;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * /trinket：快捷指令打开 Curios 饰品界面。
 */
@Mod.EventBusSubscriber(modid = MoonProjectMod.MODID)
public final class TrinketCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("trinket")
                .executes(ctx -> {
                    var source = ctx.getSource();
                    var player = source.getPlayerOrException();
                    if (TrinketHelper.isCuriosLoaded()) {
                        TrinketHelper.openCuriosScreen(player);
                    } else {
                        source.sendFailure(Component.literal("§c[月亮计划] 饰品系统需要安装 Curios API 模组！"));
                    }
                    return 1;
                }));
    }

    private TrinketCommand() {
    }
}
