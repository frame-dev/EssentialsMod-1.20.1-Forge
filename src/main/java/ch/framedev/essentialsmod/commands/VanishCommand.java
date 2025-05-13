package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class VanishCommand implements ICommand {

    public static final Set<String> vanishList = new HashSet<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("vanish")
                .requires(source -> source.hasPermission(3))
                .executes(this::execute)
                .then(Commands.literal("v").executes(this::execute)); // alias
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) return 0;

        String playerName = player.getGameProfile().getName();
        MinecraftServer server = player.getServer();
        if (server == null) {
            player.sendSystemMessage(Component.literal("Server not found.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }

        if (vanishList.contains(playerName)) {
            // Unvanish
            vanishList.remove(playerName);

            player.sendSystemMessage(Component.literal("You are now visible.")
                    .withStyle(style -> style.withColor(ChatFormatting.YELLOW)));
            player.sendSystemMessage(Component.literal("Please rejoin to be fully visible.")
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY)));

            for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                otherPlayer.connection.send(new ClientboundAddEntityPacket(player));
            }

            player.setInvisible(false);
            player.connection.teleport(player.position().x, player.position().y, player.position().z, player.getYRot(), player.getXRot());

        } else {
            // Vanish
            vanishList.add(playerName);
            player.sendSystemMessage(Component.literal("You are now vanished.")
                    .withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE)));

            Component vanishNotice = ChatUtils.getPrefix().copy().append(
                    ChatUtils.getTextComponent(new String[]{"§a", playerName, "is now vanished."},
                            new String[]{"§a", "§b", "§a"}));

            for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                if (otherPlayer != player) {
                    boolean isVanishedOrOP = vanishList.contains(otherPlayer.getName().getString()) || otherPlayer.hasPermissions(2);
                    if (!isVanishedOrOP) {
                        otherPlayer.connection.send(new ClientboundRemoveEntitiesPacket(player.getId()));
                    } else if (otherPlayer.hasPermissions(2)) {
                        otherPlayer.sendSystemMessage(vanishNotice);
                    }
                }
            }

            player.setInvisible(true);
        }

        return 1;
    }
}