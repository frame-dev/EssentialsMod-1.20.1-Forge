package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class DayNightCommand {

    public static int setDay(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        world.setDayTime(1000); // Set the time to day

        Component message = ChatUtils.getPrefix()
                .copy()
                .append(Component.literal("Time set to day!")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                .append(Component.literal(" (1000 Ticks)")
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA)));

        source.sendSuccess(() -> message, true);
        return 1;
    }

    public static int setNight(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        world.setDayTime(13000); // Set the time to night

        Component message = ChatUtils.getPrefix()
                .copy()
                .append(Component.literal("Time set to night!")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)))
                .append(Component.literal(" (13000 Ticks)")
                        .withStyle(style -> style.withColor(ChatFormatting.AQUA)));

        source.sendSuccess(() -> message, true);
        return 1;
    }

    public static int setTicks(CommandContext<CommandSourceStack> command) {
        ServerLevel world = command.getSource().getLevel();
        int ticks = IntegerArgumentType.getInteger(command, "ticks");
        world.setDayTime(ticks);

        Component message = ChatUtils.getPrefix()
                .copy()
                .append(Component.literal("Time was set to " + ticks + "!")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)));

        command.getSource().sendSuccess(() -> message, true);
        return 1;
    }
}