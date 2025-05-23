package ch.framedev.essentialsmod.utils;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;
import java.util.List;

@Mod.EventBusSubscriber
public class EssentialsConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final ForgeConfigSpec.BooleanValue enableWarps;
    public static final ForgeConfigSpec.BooleanValue useBack;
    public static final ForgeConfigSpec.BooleanValue enableLimitedHomes;
    public static final ForgeConfigSpec.IntValue limitForHomes;
    public static final ForgeConfigSpec.BooleanValue muteOtherPlayerForSelf;
    public static final ForgeConfigSpec.BooleanValue enableBackPack;
    public static final ForgeConfigSpec.BooleanValue enableBackPackSaveInConfig;
    public static final ForgeConfigSpec.BooleanValue enableSigns;
    public static final ForgeConfigSpec.BooleanValue silentJoinLeave;
    public static final ForgeConfigSpec.ConfigValue<String> configSelection;
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Essentials Mod Configuration").push("general");

        useBack = builder
                .comment("Enable the /back Command.")
                .define("enableBack", true);

        enableWarps = builder
                .comment("Enable or disable warp functionality.")
                .define("enableWarps", true);

        enableLimitedHomes = builder
                .comment("Enable or disable limited home functionality.")
                .define("enableLimitedHomes", false);

        limitForHomes = builder
                .comment("Limit the number of homes per player. Default Home will be ignored.")
               .defineInRange("limitForHomes", 5, 1, Integer.MAX_VALUE);

        muteOtherPlayerForSelf = builder
                .comment("Enable or disable the feature that allows players to mute other players for themselves.")
                        .define("muteOtherPlayerForSelf", false);

        enableBackPack = builder
                .comment("Enable or disable the Backpack Command")
                        .define("enableBackPack", false);

        enableBackPackSaveInConfig = builder
                .comment("Save Backpack settings in the config instead of a Map.")
                        .define("enableBackPackSaveInConfig", false);

        enableSigns = builder
                .comment("Enable Signs events as example [FREE]")
                        .define("enableSigns", false);

        silentJoinLeave = builder
                .comment("Enable or disable the silent join and leave messages.")
                        .define("silentJoinLeave", true);
        configSelection = builder
                .comment("Select a configuration format. Allowed: json, yaml")
                .define("configSelection", "yaml");

        builder.pop();

        builder.comment("Essentials Mod Configuration").push("warps");

        builder.pop();

        COMMON_CONFIG = builder.build();
    }

    public static void register(ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    /**
     * Loads the configuration file into the ForgeConfigSpec.
     *
     * @param configSpec The ForgeConfigSpec to set the config data.
     * @param path       The path to the configuration file.
     */
    public static void loadConfig(ForgeConfigSpec configSpec, Path path) {
        final CommentedFileConfig commentedConfig = CommentedFileConfig.builder(path).sync().autosave().build();
        commentedConfig.load();
        configSpec.setConfig(commentedConfig);
    }
}