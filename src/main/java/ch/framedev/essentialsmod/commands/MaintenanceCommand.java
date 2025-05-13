package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.OfflinePlayerUUID;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

@SuppressWarnings("unchecked")
public class MaintenanceCommand implements ICommand {

    private static final String PLAYERS_KEY = "players";
    private static final String ENABLED_KEY = "enabled";
    private static final String TAB_HEADER_KEY = "tabHeader";

    public static final Map<String,Object> DEFAULT_MAP = (Map<String, Object>) new Config().getConfig().getData().getOrDefault("maintenance", new HashMap<String,Object>());

    public static final String MOTD_MAINTENANCE = "The server is currently not available\nMaintenance!";
    public static final String MOTD_ACTIVE = "Welcome to the server!";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("maintenance")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::handlePlayer))
                .executes(this::toggleMaintenanceMode);
    }

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        context.getSource().getServer().getPlayerList().getPlayers()
                .forEach(p -> builder.suggest(p.getGameProfile().getName()));
        return builder.buildFuture();
    };

    private int handlePlayer(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "playerName");
        Config config = new Config();
        Map<String, Object> maintenanceData = (Map<String, Object>) config.getConfig().getData()
                .getOrDefault("maintenance", new HashMap<>());

        UUID uuid = OfflinePlayerUUID.getUUIDFromMojang(playerName);
        if (uuid == null) {
            context.getSource().sendFailure(Component.literal("Player not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        List<String> allowed = (List<String>) maintenanceData.getOrDefault(PLAYERS_KEY, new ArrayList<>());

        if (allowed.contains(uuid.toString())) {
            allowed.remove(uuid.toString());
            sendSuccess(context, playerName + " has been removed from the maintenance list.", ChatFormatting.GREEN);
        } else {
            allowed.add(uuid.toString());
            sendSuccess(context, playerName + " has been added to the maintenance list.", ChatFormatting.GREEN);
        }

        maintenanceData.put(PLAYERS_KEY, allowed);
        config.getConfig().set("maintenance", maintenanceData);
        config.getConfig().save();
        return 1;
    }

    private int toggleMaintenanceMode(CommandContext<CommandSourceStack> context) {
        Config config = new Config();
        Map<String, Object> maintenanceData = (Map<String, Object>) config.getConfig().getData()
                .getOrDefault("maintenance", new HashMap<>());

        boolean enabled = (boolean) maintenanceData.getOrDefault(ENABLED_KEY, false);
        boolean newStatus = !enabled;

        maintenanceData.put(ENABLED_KEY, newStatus);
        config.getConfig().set("maintenance", maintenanceData);
        config.getConfig().save();

        String status = newStatus ? "enabled" : "disabled";
        sendSuccess(context, "Maintenance mode has been " + status + ".", newStatus ? ChatFormatting.RED : ChatFormatting.GREEN);

        context.getSource().getServer().setMotd(newStatus ? MOTD_MAINTENANCE : MOTD_ACTIVE);

        String tabHeader = (String) maintenanceData.getOrDefault(TAB_HEADER_KEY, "This server is in maintenance mode!");
        Component header = Component.literal(newStatus ? tabHeader : "");
        Component footer = Component.literal("");

        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundTabListPacket(header.copy().withStyle(ChatFormatting.RED), footer));
        }

        return 1;
    }

    private void sendSuccess(CommandContext<CommandSourceStack> context, String message, ChatFormatting color) {
        context.getSource().sendSuccess(() ->
                Component.literal(message).withStyle(style -> style.withColor(color)), true);
    }
}