package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteWarpCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delwarp")
                .requires(source -> source.hasPermission(2)) // Only operators or permission level 2+
                .then(Commands.argument("warpName", StringArgumentType.word())
                        .suggests(WARP_SUGGESTIONS)
                        .executes(this::execute));
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        if (!EssentialsConfig.enableWarps.get()) {
            return 0; // Warps are disabled
        }

        try {
            ServerPlayer player = command.getSource().getPlayerOrException();
            String warpName = command.getArgument("warpName", String.class);

            if (!existsWarp(warpName)) {
                player.sendSystemMessage(Component.literal("Warp \"" + warpName + "\" not found.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
                return 0;
            }

            if (deleteWarp(warpName)) {
                player.sendSystemMessage(Component.literal("Warp \"" + warpName + "\" was successfully deleted!")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)));
                return 1;
            } else {
                player.sendSystemMessage(Component.literal("Failed to delete warp \"" + warpName + "\". Please try again.")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
                return 0;
            }

        } catch (CommandSyntaxException e) {
            EssentialsMod.getLOGGER().error("Failed to execute /delwarp command", e);
            throw new RuntimeException("Failed to execute /delwarp command", e);
        }
    }

    private boolean existsWarp(String warpName) {
        Config config = new Config();
        Map<String, Object> warpConfig = config.getConfig().getMap("warp");
        return warpConfig != null && warpConfig.containsKey(warpName);
    }

    private boolean deleteWarp(String warpName) {
        Config config = new Config();
        Map<String, Object> warpConfig = config.getConfig().getMap("warp");

        if (warpConfig != null && warpConfig.containsKey(warpName)) {
            warpConfig.remove(warpName);
            config.getConfig().set("warp", warpConfig);
            config.getConfig().save();
            return true;
        }

        return false;
    }

    private List<String> getAllWarps() {
        Config config = new Config();
        Map<String, Object> warpConfig = config.getConfig().getMap("warp");
        return warpConfig != null ? new ArrayList<>(warpConfig.keySet()) : new ArrayList<>();
    }

    private final SuggestionProvider<CommandSourceStack> WARP_SUGGESTIONS = (context, builder) -> {
        List<String> warps = getAllWarps();
        warps.forEach(builder::suggest);
        return builder.buildFuture();
    };
}