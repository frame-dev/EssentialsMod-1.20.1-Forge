package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.Location;
import ch.framedev.essentialsmod.utils.LocationsManager;
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
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("home")
                .then(Commands.argument("homeName", StringArgumentType.word())
                        .suggests(HOME_SUGGESTION)
                        .executes(this::executeWithHome))
                .executes(this::executeDefault);
    }

    private int executeWithHome(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            String homeName = StringArgumentType.getString(context, "homeName");

            if (teleportToHome(player, homeName)) {
                Component message = ChatUtils.getPrefix().copy().append(
                        ChatUtils.getTextComponent(
                                new String[]{"Teleported to home: ", homeName},
                                new String[]{"§a", "§b"}
                        )
                );
                player.sendSystemMessage(message);
            } else {
                player.sendSystemMessage(Component.literal("Home not found: " + homeName)
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeDefault(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            if (teleportToHome(player, "home")) {
                Component message = ChatUtils.getPrefix().copy().append(
                        ChatUtils.getTextComponent(
                                new String[]{"Teleported to your ", "default", " home"},
                                new String[]{"§a", "§b", "§a"}
                        )
                );
                player.sendSystemMessage(message);
            } else {
                sendNoDefaultHomeMessage(player);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public void sendNoDefaultHomeMessage(ServerPlayer player) {
        Component message = Component.literal("No default home set!")
                .withStyle(style -> style.withColor(ChatFormatting.RED))
                .append(Component.literal(" If you want to set the default home use /sethome")
                        .withStyle(style -> style.withColor(ChatFormatting.GOLD)));

        player.sendSystemMessage(message);
    }

    private boolean teleportToHome(ServerPlayer player, String homeName) {
        if (!LocationsManager.existsHome(player.getName().getString(), homeName)) {
            return false;
        }

        Location location = LocationsManager.getHome(player.getName().getString(), homeName);
        if (location == null) return false;

        if (location.getDimension() != null) {
            player.teleportTo(location.getServerLevel(player), location.getX(), location.getY(), location.getZ(), 0, 0);
        } else {
            player.teleportTo(location.getX(), location.getY(), location.getZ());
        }

        return true;
    }

    private List<String> getAllHomes(Player player) {
        Config config = new Config();
        Map<String, Object> homeData = config.getConfig().getMap("home");

        if (homeData == null || !homeData.containsKey(player.getName().getString())) {
            return new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> playerHomes = (Map<String, Object>) homeData.get(player.getName().getString());

        if (playerHomes == null) {
            return new ArrayList<>();
        }

        List<String> homes = new ArrayList<>();
        for (String home : playerHomes.keySet()) {
            if (home != null && !"null".equalsIgnoreCase(String.valueOf(playerHomes.get(home)))) {
                homes.add(home);
            }
        }

        return homes;
    }

    private final SuggestionProvider<CommandSourceStack> HOME_SUGGESTION = (context, builder) -> {
        if (context.getSource().getEntity() instanceof Player player) {
            getAllHomes(player).forEach(builder::suggest);
        }
        return builder.buildFuture();
    };
}