package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetWarpCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("setwarp")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("warpName", StringArgumentType.word())
                        .executes(this::execute));
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        if (!EssentialsConfig.enableWarps.get()) {
            context.getSource().sendFailure(
                    Component.literal("Warps are disabled in the config.")
                            .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }

        String warpName = StringArgumentType.getString(context, "warpName");

        if (!(context.getSource().getEntity() instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        String dimension = serverPlayer.level().dimension().location().toString();
        Location location = new Location(dimension, serverPlayer.getBlockX(), serverPlayer.getBlockY(), serverPlayer.getBlockZ());

        LocationsManager.setWarp(warpName, location);

        Component message = ChatUtils.getPrefix().copy().append(
                ChatUtils.getTextComponent(
                        new String[]{"Warp ", "\"" + warpName + "\"", " successfully set at your current location!"},
                        new String[]{"§a", "§b", "§a"}
                )
        );

        serverPlayer.sendSystemMessage(message);
        return 1;
    }
}