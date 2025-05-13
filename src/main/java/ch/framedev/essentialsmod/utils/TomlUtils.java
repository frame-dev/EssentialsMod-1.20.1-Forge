package ch.framedev.essentialsmod.utils;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TomlUtils {
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final TomlUtils INSTANCE;

    static {
        Pair<TomlUtils, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(TomlUtils::new);
        CONFIG_SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    // Homes and Warps Configuration Section
    private final ForgeConfigSpec.ConfigValue<List<? extends Map<String, Object>>> homes;
    private final ForgeConfigSpec.ConfigValue<List<? extends Map<String, Object>>> warps;

    public TomlUtils(ForgeConfigSpec.Builder builder) {
        builder.comment("EssentialsMod Home Configuration").push("homes");

        homes = builder
                .defineList("homes", List.of(), obj -> obj instanceof Map);
        warps = builder
                .defineList("warps", List.of(), obj -> obj instanceof Map);

        builder.pop();
    }

    public Map<String, Map<String, Object>> getHomes() {
        if (homes.get() == null) return new HashMap<>(); // Prevent NullPointerException

        return homes.get().stream()
                .filter(entry -> entry.containsKey("playerName") && entry.containsKey("homeName")) // Validate entry
                .collect(Collectors.groupingBy(
                        entry -> String.valueOf(entry.get("playerName")), // Ensure String conversion
                        Collectors.toMap(
                                e -> String.valueOf(e.get("homeName")), // Ensure String conversion
                                e -> e,
                                (existing, replacement) -> existing // Handle duplicate keys safely
                        )
                ));
    }

    public void setHomes(Map<String, Map<String, Object>> newHomes) {
        List<Map<String, Object>> homeList = newHomes.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> homeData = new HashMap<>(entry.getValue());
                    homeData.put("playerName", entry.getKey()); // Add the player name as a key
                    return homeData;
                })
                .collect(Collectors.toList());

        homes.set(homeList);
        CONFIG_SPEC.save(); // Force saving the config
    }

    public Map<String, Map<String, Object>> getWarps() {
        return warps.get().stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.get("warpName"), // Key
                        entry -> (Map<String, Object>) entry // Value
                ));
    }

    public void setWarp(Map<String, Map<String, Object>> newWarps) {
        List<Map<String, Object>> warpList = newWarps.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> warpData = new HashMap<>(entry.getValue());
                    warpData.put("warpName", entry.getKey());
                    return warpData;
                })
                .collect(Collectors.toList());
        warps.set(warpList);
    }
}