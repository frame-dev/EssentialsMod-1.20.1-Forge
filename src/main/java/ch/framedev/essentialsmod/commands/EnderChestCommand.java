package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class EnderChestCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("enderchest")
                .requires(source -> source.hasPermission(3)) // Restrict to operators (level 3+)
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithContext));
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };

    private int executeWithContext(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "playerName");
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            MinecraftServer server = source.getServer();
            ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(playerName);

            if (targetPlayer != null) {
                Component openMessage = ChatUtils.getPrefix().copy().append(
                        Component.literal("Opening Ender Chest from " + targetPlayer.getName().getString())
                                .withStyle(style -> style.withColor(ChatFormatting.YELLOW))
                );

                source.sendSuccess(() -> openMessage, true);

                currentPlayer.openMenu(new SimpleMenuProvider(
                        (id, playerInventory, playerEntity) ->
                                new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, targetPlayer.getEnderChestInventory(), 3),
                        Component.literal(targetPlayer.getName().getString() + "'s Ender Chest")
                ));
            } else {
                currentPlayer.sendSystemMessage(
                        Component.literal("This player is not online.")
                                .withStyle(style -> style.withColor(ChatFormatting.RED))
                );
            }
        }

        return 1;
    }
}