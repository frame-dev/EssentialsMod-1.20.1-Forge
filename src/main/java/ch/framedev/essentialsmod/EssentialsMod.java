package ch.framedev.essentialsmod;

import ch.framedev.essentialsmod.commands.*;
import ch.framedev.essentialsmod.events.*;
import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import ch.framedev.essentialsmod.utils.TomlUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.FileUtil;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ch.framedev.essentialsmod.commands.TempBanCommand.tempBanList;

// The value here should match an entry in the META-INF/mods.toml file

@SuppressWarnings({"InstantiationOfUtilityClass", "unchecked"})
@Mod("essentials")
public class EssentialsMod {
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static File configFile;
    public static File tomlFile;

    public EssentialsMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);
        modEventBus.addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register Events
        MinecraftForge.EVENT_BUS.register(new PlayerJoinEvent());
        MinecraftForge.EVENT_BUS.register(new InventorySyncHandler());
        MinecraftForge.EVENT_BUS.register(new BackEventHandler());
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new MuteOtherPlayerCommand.ChatEventHandler());
        if (EssentialsConfig.enableBackPack.get() && EssentialsConfig.enableBackPackSaveInConfig.get())
            MinecraftForge.EVENT_BUS.register(new BackpackCommand.BackpackEventHandler());
        MinecraftForge.EVENT_BUS.register(new TempBanCommand.BanListener());
        if (EssentialsConfig.enableSigns.get())
            MinecraftForge.EVENT_BUS.register(new SignEventHandler());

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Set up the path to the custom directory inside the config folder
        Path essentialsConfigDir = FMLPaths.CONFIGDIR.get().resolve("essentials");

        // Ensure the directory exists
        essentialsConfigDir.toFile().mkdirs(); // Ensures the directory exists

        // Define the full path for the essentials-common.toml file
        Path configPath = essentialsConfigDir.resolve("essentials-common.toml");
        configFile = new File(essentialsConfigDir.toFile(), "config.yml");
        tomlFile = new File(essentialsConfigDir.toFile(), "locations.json");

        // Register the config with Forge
        context.registerConfig(ModConfig.Type.COMMON, EssentialsConfig.COMMON_CONFIG, "essentials/essentials-common.toml");
        context.registerConfig(ModConfig.Type.COMMON, TomlUtils.CONFIG_SPEC, "essentials/homes.toml");

        // Load Config essentials-common.toml
        EssentialsConfig.loadConfig(EssentialsConfig.COMMON_CONFIG, configPath);

        // Create config.yml and update muted List in MuteCommand
        Config config = new Config();
        if (config.getConfig().getData().containsKey("muted")) {
            MuteCommand.mutedPlayers = new HashSet<>(config.getConfig().getStringList("muted"));
        }
        if (config.getConfig().getData().containsKey("tempBan")) {
            Map<String, Object> defaultConfiguration = (Map<String, Object>) config.getConfig().getData().get("tempBan");
            for (String playerName : defaultConfiguration.keySet()) {
                Map<String, Object> data = (Map<String, Object>) defaultConfiguration.get(playerName);
                TempBanCommand.BanDetails banDetails = TempBanCommand.BanDetails.fromMap(data);
                tempBanList.put(UUID.fromString(playerName), banDetails);
            }
        }
        if (!config.getConfig().getData().containsKey("maintenance") || !((Map<String, Object>) config.getConfig().getData().get("maintenance")).containsKey("tabHeader")) {
            config.getConfig().set("maintenance.tabHeader", "This server is in maintenance mode!");
            config.getConfig().save();
        }

        // Register the Config GUI
        if (FMLEnvironment.dist == Dist.CLIENT) {
            EssentialsConfigScreen.EssentialsModClient.registerConfigGui(context);
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some pre-init code
        LOGGER.info("HELLO FROM PRE INIT");
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Some example code to dispatch IMC to another mod
        InterModComms.sendTo("essentials", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // Some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().map(m -> m.messageSupplier().get()).collect(Collectors.toList()));
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        EssentialsMod.getLOGGER().info("onServerStarting");
        if (!(event.getServer() instanceof DedicatedServer server))
            return;
        Config config = new Config();
        if (!config.containsKey("defaultMotd")) {
            config.getConfig().set("defaultMotd", server.getMotd());
            config.getConfig().save();
        }
    }

    /**
     * This Method returns the Logger instance that will be used to log
     *
     * @return return the Logger instance
     */
    public static Logger getLOGGER() {
        return LOGGER;
    }

}
