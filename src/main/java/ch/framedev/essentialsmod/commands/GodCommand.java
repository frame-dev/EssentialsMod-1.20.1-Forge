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

public class GodCommand implements ICommand {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("god")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithTarget))
                .executes(this::executeSelf);
    }

    private int executeWithTarget(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "playerName");
        ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (target != null) {
            boolean enabled = !target.isInvulnerable();
            target.setInvulnerable(enabled);

            context.getSource().sendSystemMessage(ChatUtils.getPrefix().copy().append(getStatusComponent(enabled, playerName)));
            target.sendSystemMessage(ChatUtils.getPrefix().copy().append(getStatusComponent(enabled, "You")));
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Could not find player " + playerName)
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }
    }

    private int executeSelf(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            boolean enabled = !player.isInvulnerable();
            player.setInvulnerable(enabled);

            player.sendSystemMessage(ChatUtils.getPrefix().copy().append(getStatusComponent(enabled, "God mode")));
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Only a player can execute this command!")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }
    }

    private Component getStatusComponent(boolean enabled, String label) {
        String status = enabled ? "enabled" : "disabled";
        ChatFormatting statusColor = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;

        return Component.literal(label + " ").withStyle(style -> style.withColor(ChatFormatting.AQUA))
                .append(Component.literal("God mode " + status).withStyle(style -> style.withColor(statusColor)));
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };
}