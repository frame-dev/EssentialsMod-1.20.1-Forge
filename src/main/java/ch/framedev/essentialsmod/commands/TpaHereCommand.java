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
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class TpaHereCommand implements ICommand {

    private static final Map<String, String> tpHereMap = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("tpahere")
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::sendTpaHereRequest))
                .then(Commands.literal("accept")
                        .executes(this::acceptTpaHereRequest))
                .then(Commands.literal("deny")
                        .executes(this::denyTpaHereRequest));
    }

    private int sendTpaHereRequest(CommandContext<CommandSourceStack> context) {
        String targetName = StringArgumentType.getString(context, "playerName");
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer sender) {
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(targetName);

            if (target != null) {
                if (!tpHereMap.containsKey(target.getName().getString())) {
                    tpHereMap.put(target.getName().getString(), sender.getName().getString());

                    sender.sendSystemMessage(Component.literal("Sent TPAHere request to " + target.getName().getString())
                            .withStyle(style -> style.withColor(ChatFormatting.AQUA)));

                    target.sendSystemMessage(Component.literal("[" + sender.getName().getString() + "] sent a TPAHere request to you. Use ")
                            .append(Component.literal("/tpahere accept").withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                            .append(" or ")
                            .append(Component.literal("/tpahere deny").withStyle(style -> style.withColor(ChatFormatting.RED)))
                            .append("."));
                } else {
                    source.sendFailure(Component.literal("Player already has a pending TPAHere request.")
                            .withStyle(style -> style.withColor(ChatFormatting.RED)));
                }
            } else {
                source.sendFailure(Component.literal("Player not found.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }
        }
        return 1;
    }

    private int acceptTpaHereRequest(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer receiver)) return 0;

        String receiverName = receiver.getName().getString();

        if (tpHereMap.containsKey(receiverName)) {
            String senderName = tpHereMap.remove(receiverName);
            ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayerByName(senderName);

            if (sender != null) {
                receiver.teleportTo(sender.serverLevel(), sender.getX(), sender.getY(), sender.getZ(),
                        sender.getYRot(), sender.getXRot());

                receiver.sendSystemMessage(Component.literal("Accepted TPAHere request from " + senderName)
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)));

                sender.sendSystemMessage(Component.literal("[" + receiverName + "] accepted your TPAHere request.")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)));
            } else {
                receiver.sendSystemMessage(Component.literal("Requester is no longer online.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }
        } else {
            receiver.sendSystemMessage(Component.literal("You don't have any pending TPAHere requests.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
        }

        return 1;
    }

    private int denyTpaHereRequest(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer receiver)) return 0;

        String receiverName = receiver.getName().getString();

        if (tpHereMap.containsKey(receiverName)) {
            String senderName = tpHereMap.remove(receiverName);
            ServerPlayer sender = context.getSource().getServer().getPlayerList().getPlayerByName(senderName);

            if (sender != null) {
                sender.sendSystemMessage(Component.literal("[" + receiverName + "] denied your TPAHere request.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }

            receiver.sendSystemMessage(Component.literal("Denied TPAHere request from " + senderName)
                    .withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
        } else {
            receiver.sendSystemMessage(Component.literal("You don't have any pending TPAHere requests.")
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