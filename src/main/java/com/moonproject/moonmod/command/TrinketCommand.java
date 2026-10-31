package com.moonproject.moonmod.command;

import com.moonproject.moonmod.gui.TrinketMenu;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** /trinket：打开饰品界面。 */
@Mod.EventBusSubscriber(modid = com.moonproject.moonmod.MoonProjectMod.MODID)
public final class TrinketCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("trinket")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                            (id, inv, p) -> new TrinketMenu(id, inv),
                            Component.literal("异想体饰品")));
                    return 1;
                }));
    }

    private TrinketCommand() {
    }
}