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

public class HealCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("heal")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithPlayerName))
                .executes(this::executeDefault);
    }

    private int executeDefault(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer player) {
            float maxHealth = player.getMaxHealth();
            player.setHealth(maxHealth);
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(5.0F);

            Component message = ChatUtils.getPrefix().copy().append(
                    Component.literal("You have been fully healed!")
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN))
            );
            player.sendSystemMessage(message);
            return 1;
        }

        command.getSource().sendFailure(
                Component.literal("This command can only be used by players.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED))
        );
        return 0;
    }

    private int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer targetPlayer = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer == null) {
            command.getSource().sendFailure(ChatUtils.getPrefix().copy().append(
                    Component.literal("Player not found: " + playerName)
                            .withStyle(style -> style.withColor(ChatFormatting.RED))
            ));
            return 0;
        }

        float maxHealth = targetPlayer.getMaxHealth();
        targetPlayer.setHealth(maxHealth);
        targetPlayer.getFoodData().setFoodLevel(20);
        targetPlayer.getFoodData().setSaturation(5.0F);

        // Message to the target player
        Component messageToTarget = ChatUtils.getPrefix().copy().append(
                ChatUtils.getTextComponent(
                        new String[]{"You have been healed by ", command.getSource().getDisplayName().getString(), "!"},
                        new String[]{"§a", "§b", "§a"}
                )
        );
        targetPlayer.sendSystemMessage(messageToTarget);

        // Message to the command source
        Component messageToSource = ChatUtils.getPrefix().copy().append(
                ChatUtils.getTextComponent(
                        new String[]{"Healed ", targetPlayer.getGameProfile().getName()},
                        new String[]{"§a", "§b"}
                )
        );
        command.getSource().sendSuccess(() -> messageToSource, true);

        return 1;
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };
}