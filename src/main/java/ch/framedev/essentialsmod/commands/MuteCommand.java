package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class MuteCommand implements ICommand {

    public static Set<String> mutedPlayers = new HashSet<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mute")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithPlayerName));
    }

    private int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        CommandSourceStack source = command.getSource();
        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player not found: " + playerName)
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }

        Config config = new Config();
        Component sourceMessage;
        Component playerMessage;

        if (mutedPlayers.contains(playerName)) {
            // Unmute
            mutedPlayers.remove(playerName);
            sourceMessage = ChatUtils.getPrefix().copy().append(
                    ChatUtils.getTextComponent(new String[]{playerName, " has been unmuted."}, new String[]{"§b", "§a"}));
            playerMessage = Component.literal("You have been unmuted by an admin.")
                    .withStyle(style -> style.withColor(ChatFormatting.GREEN));
        } else {
            // Mute
            mutedPlayers.add(playerName);
            sourceMessage = ChatUtils.getPrefix().copy().append(
                    ChatUtils.getTextComponent(new String[]{playerName, " has been muted."}, new String[]{"§b", "§a"}));
            playerMessage = Component.literal("You have been muted by an admin.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED));
        }

        source.sendSuccess(() -> sourceMessage, true);
        targetPlayer.sendSystemMessage(playerMessage);

        config.getConfig().set("muted", mutedPlayers.stream().toList());
        config.getConfig().save();

        return 1;
    }

    public static boolean isPlayerMuted(String playerName) {
        return mutedPlayers.contains(playerName);
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