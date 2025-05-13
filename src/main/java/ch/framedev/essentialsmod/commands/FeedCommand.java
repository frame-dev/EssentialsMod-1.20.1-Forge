package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FeedCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("feed")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithPlayerName))
                .executes(this::executeDefault);
    }

    private int executeDefault(CommandContext<CommandSourceStack> command) {
        if (!(command.getSource().getEntity() instanceof ServerPlayer player)) {
            command.getSource().sendFailure(
                    ChatUtils.getPrefix().copy().append(
                            Component.literal("Only players can use this command!")
                                    .withStyle(style -> style.withColor(ChatFormatting.RED))
                    )
            );
            return 0;
        }

        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5.0F);

        Component message = ChatUtils.getPrefix().copy().append(
                ChatUtils.getColoredTextComponent("Your food bar has been fully replenished!", ChatFormatting.GREEN)
        );
        player.sendSystemMessage(message);
        return 1;
    }

    private int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer targetPlayer = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer != null) {
            targetPlayer.getFoodData().setFoodLevel(20);
            targetPlayer.getFoodData().setSaturation(5.0F);

            Component messageToTarget = ChatUtils.getPrefix().copy().append(
                    ChatUtils.getTextComponent(
                            new String[]{"Your food bar has been fully replenished by ", command.getSource().getDisplayName().getString(), "!"},
                            new String[]{"§a", "§b", "§a"}
                    )
            );
            targetPlayer.sendSystemMessage(messageToTarget);

            Component messageToSource = ChatUtils.getPrefix().copy().append(
                    ChatUtils.getTextComponent(
                            new String[]{"You replenished food for ", targetPlayer.getName().getString(), "."},
                            new String[]{"§a", "§b", "§a"}
                    )
            );
            command.getSource().sendSuccess(() -> messageToSource, true);
            return 1;
        } else {
            command.getSource().sendFailure(
                    Component.literal("Player not found: " + playerName)
                            .withStyle(style -> style.withColor(ChatFormatting.RED))
            );
            return 0;
        }
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };
}