package ch.framedev.essentialsmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class TpaCommand implements ICommand {

    private static final Map<String, String> tpaMap = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("tpa")
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::sendTpaRequest))
                .then(Commands.literal("accept")
                        .executes(this::acceptTpaRequest))
                .then(Commands.literal("deny")
                        .executes(this::denyTpaRequest));
    }

    private int sendTpaRequest(CommandContext<CommandSourceStack> context) {
        String targetName = StringArgumentType.getString(context, "playerName");
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer sender) {
            MinecraftServer server = source.getServer();
            ServerPlayer receiver = server.getPlayerList().getPlayerByName(targetName);

            if (receiver != null) {
                if (!tpaMap.containsKey(receiver.getName().getString())) {
                    tpaMap.put(receiver.getName().getString(), sender.getName().getString());

                    sender.sendSystemMessage(Component.literal("Sent TPA request to " + receiver.getName().getString())
                            .withStyle(style -> style.withColor(ChatFormatting.AQUA)));

                    receiver.sendSystemMessage(Component.literal("[" + sender.getName().getString() + "] sent a TPA request to you. Use ")
                            .append(Component.literal("/tpa accept").withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                            .append(" or ")
                            .append(Component.literal("/tpa deny").withStyle(style -> style.withColor(ChatFormatting.RED)))
                            .append("."));
                } else {
                    source.sendFailure(Component.literal("Player already has a pending TPA request.")
                            .withStyle(style -> style.withColor(ChatFormatting.RED)));
                }
            } else {
                source.sendFailure(Component.literal("Player not found.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }
        }

        return 1;
    }

    private int acceptTpaRequest(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer receiver)) return 0;

        String receiverName = receiver.getName().getString();

        if (tpaMap.containsKey(receiverName)) {
            String senderName = tpaMap.remove(receiverName);
            ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayerByName(senderName);

            if (sender != null) {
                sender.teleportTo(receiver.serverLevel(), receiver.getX(), receiver.getY(), receiver.getZ(),
                        receiver.getYRot(), receiver.getXRot());

                receiver.sendSystemMessage(Component.literal("Accepted TPA request from " + sender.getName().getString())
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)));

                sender.sendSystemMessage(Component.literal("[" + receiverName + "] accepted your TPA request.")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)));
            } else {
                receiver.sendSystemMessage(Component.literal("Requester is no longer online.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }
        } else {
            receiver.sendSystemMessage(Component.literal("You don't have any pending TPA requests.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
        }

        return 1;
    }

    private int denyTpaRequest(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer receiver)) return 0;

        String receiverName = receiver.getName().getString();

        if (tpaMap.containsKey(receiverName)) {
            String senderName = tpaMap.remove(receiverName);
            ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayerByName(senderName);

            if (sender != null) {
                sender.sendSystemMessage(Component.literal("[" + receiverName + "] denied your TPA request.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }

            receiver.sendSystemMessage(Component.literal("Denied TPA request from " + senderName)
                    .withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
        } else {
            receiver.sendSystemMessage(Component.literal("You don't have any pending TPA requests.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
        }

        return 1;
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (!VanishCommand.vanishList.contains(player.getGameProfile().getName())) {
                builder.suggest(player.getGameProfile().getName());
            }
        }
        return builder.buildFuture();
    };
}