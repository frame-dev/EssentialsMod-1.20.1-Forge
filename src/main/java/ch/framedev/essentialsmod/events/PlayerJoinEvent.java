package ch.framedev.essentialsmod.events;

import ch.framedev.essentialsmod.commands.MaintenanceCommand;
import ch.framedev.essentialsmod.commands.VanishCommand;
import ch.framedev.essentialsmod.utils.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
@Mod.EventBusSubscriber(modid = "essentials")
public class PlayerJoinEvent {

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getServer() == null) return;
        if (VanishCommand.vanishList.contains(player.getName().getString())) {
            player.getServer().getPlayerList().remove(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getServer() == null) return;
            PlayerList playerList = player.getServer().getPlayerList();

            if (VanishCommand.vanishList.contains(player.getName().getString())) {
                for (ServerPlayer otherPlayer : playerList.getPlayers()) {
                    if (!otherPlayer.equals(player) && !VanishCommand.vanishList.contains(otherPlayer.getName().getString()) && !otherPlayer.hasPermissions(2)) {
                        otherPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
                    }
                }
                playerList.getPlayers().remove(player);
            } else {
                for (ServerPlayer otherPlayer : playerList.getPlayers()) {
                    if (!otherPlayer.equals(player) && VanishCommand.vanishList.contains(otherPlayer.getName().getString()) && !player.hasPermissions(2)) {
                        player.connection.send(new ClientboundRemoveEntitiesPacket(otherPlayer.getId()));
                    }
                }
            }

            Config config = new Config();
            Map<String, Object> defaultMap = (Map<String, Object>) config.getConfig().getData().getOrDefault("maintenance", new HashMap<>());
            boolean isMaintenanceMode = (boolean) defaultMap.getOrDefault("enabled", false);
            if (isMaintenanceMode) {
                List<String> uuids = (List<String>) defaultMap.getOrDefault("players", new ArrayList<>());
                if (uuids.contains(player.getUUID().toString())) {
                    String tabHeader = (String) defaultMap.getOrDefault("tabHeader", "This server is in maintenance mode!");
                    for (ServerPlayer serverPlayer : player.getServer().getPlayerList().getPlayers()) {
                        serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundTabListPacket(
                                Component.literal(tabHeader).withStyle(ChatFormatting.RED),
                                Component.literal("")
                        ));
                    }
                } else {
                    player.sendSystemMessage(Component.literal("Server is in maintenance mode."));
                    player.connection.disconnect(Component.literal("Server is in maintenance mode!").withStyle(ChatFormatting.RED));
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    private static DedicatedServer server;
    private static Boolean cachedMaintenanceMode = null;

    @SubscribeEvent
    public static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        MinecraftServer eventServer = event.getServer();
        if (eventServer instanceof DedicatedServer) {
            server = (DedicatedServer) eventServer;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (server != null) updateMotdIfNeeded();
    }

    private static void updateMotdIfNeeded() {
        Map<String, Object> maintenanceMap = MaintenanceCommand.DEFAULT_MAP;
        Config config = new Config();

        boolean maintenanceEnabled = (boolean) maintenanceMap.getOrDefault("enabled", false);

        if (cachedMaintenanceMode == null || cachedMaintenanceMode != maintenanceEnabled) {
            cachedMaintenanceMode = maintenanceEnabled;
            String newMotd = maintenanceEnabled
                    ? "\u00a7c\u00a7lThe server is currently under maintenance!\nPlease try again later."
                    : config.getConfig().getString("defaultMotd");

            server.setMotd(newMotd);
            ServerStatus status = server.getStatus();
            updateServerProperties(newMotd);
            System.out.println("Updated MOTD: " + newMotd);
        }
    }

    private static void updateServerProperties(String value) {
        File propertiesFile = new File(server.getServerDirectory(), "server.properties");
        File backupFile = new File(server.getServerDirectory(), "server.properties.bak");
        Properties properties = new Properties();

        if (!propertiesFile.exists()) {
            System.err.println("[EssentialsMod] server.properties file not found! Skipping MOTD update.");
            return;
        }

        try (FileInputStream in = new FileInputStream(propertiesFile)) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("[EssentialsMod] Failed to load server.properties: " + e.getMessage());
            return;
        }

        try {
            if (!backupFile.exists()) {
                if (!propertiesFile.renameTo(backupFile)) {
                    System.err.println("[EssentialsMod] Failed to create backup of server.properties.");
                }
            }
        } catch (Exception e) {
            System.err.println("[EssentialsMod] Failed to create backup: " + e.getMessage());
        }

        try {
            properties.setProperty("motd", parseUnicodeEscapeSequences(value.replaceAll("§", "")));
            try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
                properties.store(out, "Updated by EssentialsMod");
            }
            System.out.println("[EssentialsMod] Successfully updated server.properties with new MOTD.");
        } catch (IOException e) {
            System.err.println("[EssentialsMod] Failed to update server.properties: " + e.getMessage());
        }
    }

    public static String parseUnicodeEscapeSequences(String input) {
        Pattern unicodePattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
        Matcher matcher = unicodePattern.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String unicodeChar = String.valueOf((char) Integer.parseInt(matcher.group(1), 16));
            matcher.appendReplacement(result, unicodeChar);
        }
        matcher.appendTail(result);
        return result.toString().replace("\\n", "\n");
    }
}