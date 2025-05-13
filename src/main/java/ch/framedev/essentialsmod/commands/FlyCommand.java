package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FlyCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("fly")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithPlayerName)
                        .then(Commands.argument("speed", FloatArgumentType.floatArg(0.1f, 10f))
                                .executes(this::executeWithPlayerNameSpeed)))
                .executes(this::executeDefault);
    }

    private int executeWithPlayerNameSpeed(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        float speed = FloatArgumentType.getFloat(command, "speed");
        ServerPlayer player = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (player == null) {
            command.getSource().sendFailure(ChatUtils.getPrefix().copy().append(
                    Component.literal("Player not found!").withStyle(style -> style.withColor(ChatFormatting.RED))
            ));
            return 0;
        }

        setFlySpeed(player, speed);
        command.getSource().sendSuccess(() -> ChatUtils.getPrefix().copy().append(
                Component.literal("Set flight speed for " + player.getGameProfile().getName() + " to " + speed)
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN))
        ), true);
        return 1;
    }

    private int executeWithPlayerName(CommandContext<CommandSourceStack> command) {
        String playerName = StringArgumentType.getString(command, "playerName");
        ServerPlayer player = command.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (player == null) {
            command.getSource().sendFailure(ChatUtils.getPrefix().copy().append(
                    Component.literal("Player not found!").withStyle(style -> style.withColor(ChatFormatting.RED))
            ));
            return 0;
        }

        boolean enabling = !player.getAbilities().mayfly;
        toggleFlight(player, enabling);

        player.sendSystemMessage(ChatUtils.getPrefix().copy().append(getTextByStatus(enabling)));
        command.getSource().sendSuccess(() -> ChatUtils.getPrefix().copy().append(
                getTextByStatusOther(enabling, player.getGameProfile().getName())), true);
        return 1;
    }

    private int executeDefault(CommandContext<CommandSourceStack> command) {
        if (!(command.getSource().getEntity() instanceof ServerPlayer player)) {
            command.getSource().sendFailure(ChatUtils.getPrefix().copy().append(
                    Component.literal("Only players can use this command!")
                            .withStyle(style -> style.withColor(ChatFormatting.RED))
            ));
            return 0;
        }

        boolean enabling = !player.getAbilities().mayfly;
        toggleFlight(player, enabling);

        player.sendSystemMessage(ChatUtils.getPrefix().copy().append(getTextByStatus(enabling)));
        return 1;
    }

    private static void toggleFlight(ServerPlayer player, boolean enable) {
        player.getAbilities().mayfly = enable;
        player.getAbilities().flying = enable;
        player.onUpdateAbilities();
    }

    private static void setFlySpeed(ServerPlayer player, float speed) {
        player.getAbilities().setFlyingSpeed(speed / 10.0f);
        player.onUpdateAbilities();

        player.sendSystemMessage(
                Component.literal("Fly speed set to " + player.getAbilities().getFlyingSpeed())
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA))
        );
    }

    private static Component getTextByStatus(boolean active) {
        String status = active ? "enabled!" : "disabled!";
        return ChatUtils.getTextComponent(new String[]{"Flying ", status}, new String[]{"§a", "§6"});
    }

    private static Component getTextByStatusOther(boolean active, String playerName) {
        String status = active ? "enabled!" : "disabled!";
        return ChatUtils.getTextComponent(new String[]{
                "Flying for ", playerName, " is ", status
        }, new String[]{"§a", "§6", "§a", "§6"});
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };
}