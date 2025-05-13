package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MuteOtherPlayerCommand implements ICommand {

    public static final Map<String, Set<String>> playerMuteMap = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("muteother")
                .requires(source -> source.hasPermission(0))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .executes(this::execute));
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command!")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }

        String targetName = StringArgumentType.getString(context, "playerName");

        if (targetName.equalsIgnoreCase(player.getName().getString())) {
            player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("You cannot mute yourself.").withStyle(style -> style.withColor(ChatFormatting.RED)))
            );
            return 0;
        }

        playerMuteMap.putIfAbsent(player.getName().getString(), new HashSet<>());
        Set<String> muted = playerMuteMap.get(player.getName().getString());

        if (muted.contains(targetName)) {
            muted.remove(targetName);
            player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal(targetName + " has been unmuted for you.")
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN)))
            );
        } else {
            muted.add(targetName);
            player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal(targetName + " has been muted for you.")
                            .withStyle(style -> style.withColor(ChatFormatting.RED)))
            );
        }

        return 1;
    }

    @Mod.EventBusSubscriber(modid = "essentials")
    public static class ChatEventHandler {

        @SubscribeEvent
        public static void onPlayerChat(ServerChatEvent event) {
            ServerPlayer sender = event.getPlayer();
            String senderName = sender.getName().getString();
            MinecraftServer server = sender.getServer();

            if (server == null) return;

            Component message = event.getMessage();

            // Send to each player except those who muted the sender
            for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
                String recipientName = recipient.getName().getString();

                if (playerMuteMap.getOrDefault(recipientName, Set.of()).contains(senderName)) {
                    continue; // skip muted
                }

                recipient.sendSystemMessage(message);
            }

            // Log the message to console
            EssentialsMod.getLOGGER().info("{}: {}", senderName, event.getMessage());
            event.setCanceled(true); // cancel vanilla broadcasting
        }
    }
}