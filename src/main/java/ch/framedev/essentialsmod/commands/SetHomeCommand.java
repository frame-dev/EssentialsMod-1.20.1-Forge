package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import ch.framedev.essentialsmod.utils.Location;
import ch.framedev.essentialsmod.utils.LocationsManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class SetHomeCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("sethome")
                .then(Commands.argument("homeName", StringArgumentType.word())
                        .executes(this::executeWithHomeName))
                .executes(this::executeDefault);
    }

    private int executeWithHomeName(CommandContext<CommandSourceStack> context) {
        String homeName = StringArgumentType.getString(context, "homeName");

        if (!(context.getSource().getEntity() instanceof Player player)) return 0;

        if (EssentialsConfig.enableLimitedHomes.get()) {
            int limit = EssentialsConfig.limitForHomes.get();
            if (LocationsManager.getHomes(player.getName().getString(), true).size() >= limit) {
                player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                        Component.literal("You have reached the maximum number of homes (" + limit + ").")
                                .withStyle(style -> style.withColor(ChatFormatting.RED))));
                return 0;
            }
        }

        String playerName = player.getName().getString();
        Component message = ChatUtils.getPrefix().copy().append(
                ChatUtils.getTextComponent(new String[]{"Home set with name", "\"" + homeName + "\""}, new String[]{"§a", "§b"})
        );
        player.sendSystemMessage(message);

        String dimension = player.level().dimension().location().toString();
        LocationsManager.setHome(playerName, new Location(dimension, player.getBlockX(), player.getBlockY(), player.getBlockZ()), homeName);

        return Command.SINGLE_SUCCESS;
    }

    private int executeDefault(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof Player player)) return 0;

        String playerName = player.getName().getString();
        player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                Component.literal("Home Set").withStyle(style -> style.withColor(ChatFormatting.GREEN)))
        );

        String dimension = player.level().dimension().location().toString();
        LocationsManager.setHome(playerName, new Location(dimension, player.getBlockX(), player.getBlockY(), player.getBlockZ()), null);

        return Command.SINGLE_SUCCESS;
    }
}