package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RepairCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("repair")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithPlayerName))
                .executes(this::executeDefault);
    }

    private int executeDefault(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        ItemStack stack = player.getMainHandItem();

        if (stack.isEmpty()) {
            player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("You are not holding any item!")
                            .withStyle(style -> style.withColor(ChatFormatting.RED))));
            return 0;
        }

        if (!stack.isDamageableItem()) {
            player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("The item in your hand cannot be repaired!")
                            .withStyle(style -> style.withColor(ChatFormatting.RED))));
            return 0;
        }

        stack.setDamageValue(0);

        player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                Component.literal("Your item has been repaired!")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN))));
        return Command.SINGLE_SUCCESS;
    }

    private int executeWithPlayerName(CommandContext<CommandSourceStack> context) {
        String playerName = context.getArgument("playerName", String.class);
        ServerPlayer targetPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer == null) {
            context.getSource().sendFailure(Component.literal("Player not found: " + playerName)
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }

        ItemStack stack = targetPlayer.getMainHandItem();

        if (stack.isEmpty()) {
            context.getSource().sendSuccess(() -> ChatUtils.getPrefix().copy().append(
                    Component.literal(playerName + " is not holding any item!")
                            .withStyle(style -> style.withColor(ChatFormatting.RED))), true);
            return 0;
        }

        if (!stack.isDamageableItem()) {
            context.getSource().sendSuccess(() -> ChatUtils.getPrefix().copy().append(
                    Component.literal("The item in " + playerName + "'s hand cannot be repaired!")
                            .withStyle(style -> style.withColor(ChatFormatting.RED))), true);
            return 0;
        }

        stack.setDamageValue(0);

        targetPlayer.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                Component.literal("Your item has been repaired by " + context.getSource().getDisplayName().getString() + "!")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN))));

        context.getSource().sendSuccess(() -> ChatUtils.getPrefix().copy().append(
                Component.literal("Repaired the item for " + playerName + "!")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN))), true);

        return Command.SINGLE_SUCCESS;
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