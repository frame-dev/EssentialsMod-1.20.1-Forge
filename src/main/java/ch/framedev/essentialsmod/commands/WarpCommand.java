package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarpCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("warp")
                .then(Commands.argument("warpName", StringArgumentType.word())
                        .suggests(WARP_SUGGESTIONS)
                        .executes(this::execute));
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        if (!EssentialsConfig.enableWarps.get()) {
            context.getSource().sendFailure(Component.literal("Warps are disabled in the configuration.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }

        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String warpName = StringArgumentType.getString(context, "warpName");

            if (teleportToWarp(player, warpName)) {
                Component message = ChatUtils.getPrefix().copy().append(
                        ChatUtils.getTextComponent(
                                new String[]{"Teleported to warp ", "\"" + warpName + "\"", "."},
                                new String[]{"§a", "§b", "§a"}
                        )
                );
                player.sendSystemMessage(message);
                return 1;
            } else {
                player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                        Component.literal("Warp \"" + warpName + "\" not found.")
                                .withStyle(style -> style.withColor(ChatFormatting.RED)))
                );
                return 0;
            }

        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("An error occurred while executing the /warp command.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }
    }

    private boolean teleportToWarp(ServerPlayer player, String warpName) {
        Config config = new Config();
        Map<String, Object> warps = config.getConfig().getMap("warp");

        if (warps == null || !warps.containsKey(warpName)) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> warpData = (Map<String, Object>) warps.get(warpName);

        if (!warpData.containsKey("x") || !warpData.containsKey("y") || !warpData.containsKey("z") || !warpData.containsKey("dimension")) {
            player.sendSystemMessage(Component.literal("This warp is misconfigured. Please set it again.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return false;
        }

        int x = (int) warpData.get("x");
        int y = (int) warpData.get("y");
        int z = (int) warpData.get("z");
        String dimensionStr = (String) warpData.get("dimension");

        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionStr));
        MinecraftServer server = player.getServer();

        if (server == null) return false;

        ServerLevel level = server.getLevel(dimensionKey);
        if (level == null) return false;

        player.teleportTo(level, x + 0.5, y, z + 0.5, 0f, 0f);
        return true;
    }

    private List<String> getAllWarps() {
        Config config = new Config();
        Map<String, Object> warps = config.getConfig().getMap("warp");
        return warps == null ? new ArrayList<>() : new ArrayList<>(warps.keySet());
    }

    private final SuggestionProvider<CommandSourceStack> WARP_SUGGESTIONS = (context, builder) -> {
        for (String warp : getAllWarps()) {
            builder.suggest(warp);
        }
        return builder.buildFuture();
    };
}