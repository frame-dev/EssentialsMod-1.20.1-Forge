package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DeleteHomeCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delhome")
                .then(Commands.argument("homeName", StringArgumentType.word())
                        .suggests(HOME_SUGGESTION)
                        .executes(this::executeWithHome))
                .executes(this::executeDefault);
    }

    private int executeWithHome(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            String homeName = StringArgumentType.getString(context, "homeName");
            if (deleteHome(player, homeName)) {
                Component textComponent = ChatUtils.getTextComponent(
                        new String[]{"Home: ", homeName, " deleted!"},
                        new String[]{"§a", "§b", "§a"}
                );
                player.sendSystemMessage(ChatUtils.getPrefix().copy().append(textComponent));
            } else {
                player.sendSystemMessage(Component.literal("Home not found: " + homeName)
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private int executeDefault(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            if (deleteHome(player, "home")) {
                Component textComponent = ChatUtils.getTextComponent(
                        new String[]{"Default ", "home deleted!"},
                        new String[]{"§b", "§a"}
                );
                player.sendSystemMessage(ChatUtils.getPrefix().copy().append(textComponent));
            } else {
                player.sendSystemMessage(Component.literal("No default home set!")
                        .withStyle(style -> style.withColor(ChatFormatting.RED)));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private boolean deleteHome(Player player, String homeName) {
        Config config = new Config();
        String playerKey = "home." + player.getName().getString() + "." + homeName;

        // If the home doesn't have coordinates saved, consider it non-existent
        if (!config.getConfig().containsKey(playerKey + ".x")) {
            return false;
        }

        Map<String, Object> homeMap = config.getConfig().getMap("home." + player.getName().getString());
        if (homeMap != null) {
            homeMap.remove(homeName);
            config.getConfig().set("home." + player.getName().getString(), homeMap);
            config.getConfig().save();
            return true;
        }

        return false;
    }

    private List<String> getAllHomes(Player player) {
        Config config = new Config();
        Map<String, Object> allHomes = config.getConfig().getMap("home");

        if (allHomes == null || !allHomes.containsKey(player.getName().getString())) {
            return new ArrayList<>();
        }

        Map<String, Object> playerHomes = (Map<String, Object>) allHomes.get(player.getName().getString());
        if (playerHomes == null) {
            return new ArrayList<>();
        }

        List<String> homeNames = new ArrayList<>();
        for (String key : playerHomes.keySet()) {
            if (key != null && !"null".equalsIgnoreCase(key)) {
                homeNames.add(key);
            }
        }

        return homeNames;
    }

    private final SuggestionProvider<CommandSourceStack> HOME_SUGGESTION = (context, builder) -> {
        if (context.getSource().getEntity() instanceof Player player) {
            List<String> homes = getAllHomes(player);
            for (String home : homes) {
                builder.suggest(home);
            }
        }
        return builder.buildFuture();
    };
}