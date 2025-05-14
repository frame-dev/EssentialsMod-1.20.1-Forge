package ch.framedev.essentialsmod.utils;



/*
 * ch.framedev.essentialsmod.commands
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 13.01.2025 23:18
 */

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocationsManager {

    private static final Map<String, Location> homeMap = new HashMap<>();
    private static final boolean useConfigForHome = new Config().getBoolean("useConfigForHomes");

    public static boolean existsSpawn() {
        Config config = new Config();
        return config.getConfig().containsKey("spawn.dimension") && config.getConfig().containsKey("spawn.x") && config.getConfig().containsKey("spawn.y") && config.getConfig().containsKey("spawn.z");
    }

    public static void setSpawn(@NotNull Location location) {
        // Save the spawn location in the config
        Config config = new Config();
        config.getConfig().set("spawn.dimension", location.getDimension());
        config.getConfig().set("spawn.x", location.getX());
        config.getConfig().set("spawn.y", location.getY());
        config.getConfig().set("spawn.z", location.getZ());
        config.getConfig().save();
    }

    public static @Nullable Location getSpawn() {
        if (!existsSpawn()) return null;
        Config config = new Config();
        String dimension = config.getConfig().getString("spawn.dimension");
        int x = config.getConfig().getInt("spawn.x");
        int y = config.getConfig().getInt("spawn.y");
        int z = config.getConfig().getInt("spawn.z");
        return new Location(dimension, x, y, z);
    }

    public static boolean existsWarp(@NotNull String warpName) {
        if (EssentialsConfig.configSelection.get().equalsIgnoreCase("yaml")) {
            Config config = new Config();
            return config.getConfig().containsKey("warp." + warpName + ".x");
        } else {
            Map<String, Map<String, Object>> warps = TomlUtils.INSTANCE.getWarps();
            return warps != null && warps.containsKey(warpName);
        }
    }

    public static void setWarp(@NotNull String warpName, @NotNull Location location) {
        if (EssentialsConfig.configSelection.get().equalsIgnoreCase("yaml")) {
            Config config = new Config();
            config.getConfig().set("warp." + warpName + ".dimension", location.getDimension());
            config.getConfig().set("warp." + warpName + ".x", location.getX());
            config.getConfig().set("warp." + warpName + ".y", location.getY());
            config.getConfig().set("warp." + warpName + ".z", location.getZ());
            config.getConfig().save();
        } else {
            Map<String, Map<String, Object>> warps = TomlUtils.INSTANCE.getWarps();
            if (warps == null) warps = new HashMap<>(); // Ensure warps are initialized

            Map<String, Object> locationData = new HashMap<>();
            locationData.put("dimension", location.getDimension());
            locationData.put("x", location.getX());
            locationData.put("y", location.getY());
            locationData.put("z", location.getZ());

            warps.put(warpName, locationData);
            TomlUtils.INSTANCE.setWarp(warps);
            TomlUtils.CONFIG_SPEC.save(); // Force save
        }
    }

    @SuppressWarnings("unchecked")
    public static @Nullable Location getWarp(@NotNull String warpName) {
        if (!existsWarp(warpName)) return null;

        if (EssentialsConfig.configSelection.get().equalsIgnoreCase("yaml")) {
            Config config = new Config();
            String dimension = config.getConfig().getString("warp." + warpName + ".dimension");
            int x = config.getConfig().getInt("warp." + warpName + ".x");
            int y = config.getConfig().getInt("warp." + warpName + ".y");
            int z = config.getConfig().getInt("warp." + warpName + ".z");

            return new Location(dimension, x, y, z);
        } else {
            if (!existsWarp(warpName)) return null;
            Map<String, Map<String, Object>> warps = TomlUtils.INSTANCE.getWarps();

            if (warps == null || !warps.containsKey(warpName)) return null; // Ensure warps exist

            Map<String, Object> warpData = warps.get(warpName);
            if (warpData == null) return null;

            String dimension = (String) warpData.get("dimension");
            int x = (int) warpData.get("x");
            int y = (int) warpData.get("y");
            int z = (int) warpData.get("z");

            return new Location(dimension, x, y, z);
        }
    }

    public static boolean existsHome(@NotNull String playerName, String home) {
        if (useConfigForHome) {
            if (EssentialsConfig.configSelection.get().equalsIgnoreCase("yaml")) {
                String saveKey = "home." + playerName + ".";
                if (home == null) home = "home";

                Config config = new Config();
                return config.getConfig().containsKey(saveKey + home + ".x");
            } else {
                Map<String, Map<String, Object>> homes = TomlUtils.INSTANCE.getHomes();
                if (home == null) home = "home";
                return homes.containsKey(playerName) && homes.get(playerName).containsKey(home);
            }
        } else {
            return homeMap.containsKey(playerName + "." + home);
        }
    }

    public static void setHome(@NotNull String playerName, @NotNull Location location, String home) {
        if (useConfigForHome) {
            if (EssentialsConfig.configSelection.get().equalsIgnoreCase("yaml")) {
                String saveKey = "home." + playerName + ".";
                if (home == null) home = "home";

                Config config = new Config();
                config.getConfig().set(saveKey + home + ".dimension", location.getDimension());
                config.getConfig().set(saveKey + home + ".x", location.getX());
                config.getConfig().set(saveKey + home + ".y", location.getY());
                config.getConfig().set(saveKey + home + ".z", location.getZ());
                config.getConfig().save();
            } else {
                if (home == null) home = "home";

                Map<String, Object> homeData = new HashMap<>();
                homeData.put("dimension", location.getDimension());
                homeData.put("x", location.getX());
                homeData.put("y", location.getY());
                homeData.put("z", location.getZ());
                homeData.put("homeName", home); // Include home name

                Map<String, Map<String, Object>> homes = TomlUtils.INSTANCE.getHomes();
                homes.computeIfAbsent(playerName, k -> new HashMap<>()).put(home, homeData);

                TomlUtils.INSTANCE.setHomes(homes);
                System.out.println(homes);
            }
        } else {
            homeMap.put(playerName + "." + home, location);
        }
    }

    @SuppressWarnings("unchecked")
    public static @Nullable Location getHome(@NotNull String playerName, String home) {
        if (useConfigForHome) {
            if (EssentialsConfig.configSelection.get().equalsIgnoreCase("yaml")) {
                String saveKey = "home." + playerName + ".";
                if (home == null) home = "home";

                if (!existsHome(playerName, home)) return null;

                Config config = new Config();
                if (!config.containsKey(saveKey + home + ".dimension")) {
                    String dimension = config.getConfig().getString(saveKey + home + ".dimension");
                    int x = config.getConfig().getInt(saveKey + home + ".x");
                    int y = config.getConfig().getInt(saveKey + home + ".y");
                    int z = config.getConfig().getInt(saveKey + home + ".z");
                    return new Location(dimension, x, y, z);
                } else {
                    int x = config.getConfig().getInt(saveKey + home + ".x");
                    int y = config.getConfig().getInt(saveKey + home + ".y");
                    int z = config.getConfig().getInt(saveKey + home + ".z");
                    return new Location(null, x, y, z);
                }
            } else {
                if (!existsHome(playerName, home)) return null;
                if (home == null) home = "home";
                Map<String, Map<String, Object>> homes = TomlUtils.INSTANCE.getHomes();

                if (!homes.containsKey(playerName) || !homes.get(playerName).containsKey(home)) {
                    return null; // Home doesn't exist
                }

                Map<String, Object> homeData = (Map<String, Object>) homes.get(playerName).get(home);

                // Retrieve values
                String dimension = (String) homeData.get("dimension");
                int x = (int) homeData.get("x");
                int y = (int) homeData.get("y");
                int z = (int) homeData.get("z");

                return new Location(dimension, x, y, z);
            }
        } else {
            return homeMap.getOrDefault(playerName + "." + home, null);
        }
    }

    public static List<String> getWarps() {
        Config config = new Config();
        Map<String, Object> warps = config.getConfig().getMap("warp");
        if (warps == null) return new ArrayList<>();
        return new ArrayList<>(warps.keySet());
    }

    @SuppressWarnings("unchecked")
    public static List<String> getHomes(String playerName, boolean ignoreDefault) {
        List<String> homes = new ArrayList<>();

        if (useConfigForHome) {
            if (EssentialsConfig.configSelection.get().equalsIgnoreCase("yaml")) {
                Config config = new Config();
                Map<String, Object> defaultConfig = config.getConfig().getMap("home");
                if (defaultConfig == null || !defaultConfig.containsKey(playerName)) return homes;

                Map<String, Object> homeConfig = (Map<String, Object>) defaultConfig.get(playerName);
                if (homeConfig == null) return homes;

                for (String homeName : homeConfig.keySet()) {
                    if (!(ignoreDefault && "home".equalsIgnoreCase(homeName))) {
                        homes.add(homeName);
                    }
                }
            } else {
                Map<String, Map<String, Object>> homesMap = TomlUtils.INSTANCE.getHomes();
                if (homesMap == null || !homesMap.containsKey(playerName)) return homes;

                return homesMap.get(playerName).keySet().stream()
                        .filter(home -> !(ignoreDefault && "home".equalsIgnoreCase(home)))
                        .collect(Collectors.toList());
            }
        } else {
            homes = homeMap.keySet().stream()
                    .map(location -> location.split("\\."))
                    .filter(parts -> parts.length >= 2 && parts[0].equalsIgnoreCase(playerName))
                    .map(parts -> parts[1])
                    .filter(home -> !(ignoreDefault && "home".equalsIgnoreCase(home)))
                    .collect(Collectors.toList());
        }

        return homes;
    }
}
