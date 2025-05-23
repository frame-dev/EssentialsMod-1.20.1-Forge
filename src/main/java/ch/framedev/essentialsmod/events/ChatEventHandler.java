package ch.framedev.essentialsmod.events;

import ch.framedev.essentialsmod.commands.MuteCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "essentials") // Replace with your mod's ID
public class ChatEventHandler {

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event) {
        String playerName = event.getPlayer().getName().getString();

        if (MuteCommand.isPlayerMuted(playerName)) {
            // Cancel the chat event and notify the player
            event.setCanceled(true);
            event.setResult(Event.Result.DENY);
            event.getPlayer().sendSystemMessage(Component.literal("You are muted and cannot send messages.").withStyle(ChatFormatting.RED));
        }
    }
}
