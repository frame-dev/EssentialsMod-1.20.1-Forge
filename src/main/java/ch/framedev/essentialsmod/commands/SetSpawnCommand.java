package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SetSpawnCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("setspawn")
                .requires(source -> source.hasPermission(3))
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }

        if (!player.hasPermissions(3)) {
            return 0;
        }

        try {
            ServerLevel world = context.getSource().getLevel();
            Config config = new Config();

            // Save spawn data
            config.getConfig().set("spawn.dimension", player.level().dimension().location().toString());
            config.getConfig().set("spawn.x", player.getBlockX());
            config.getConfig().set("spawn.y", player.getBlockY());
            config.getConfig().set("spawn.z", player.getBlockZ());
            config.getConfig().save();

            BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
            world.setDefaultSpawnPos(pos, 0); // Yaw = 0

            Component message = ChatUtils.getPrefix().copy().append(
                    Component.literal("World spawn set to your current position: " +
                                      player.getBlockX() + ", " + player.getBlockY() + ", " + player.getBlockZ())
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN))
            );
            player.sendSystemMessage(message);

            return 1;
        } catch (Exception e) {
            EssentialsMod.getLOGGER().error("Failed to set spawn.", e);
            player.sendSystemMessage(Component.literal("An error occurred while setting the world spawn.")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }
    }
}