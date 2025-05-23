package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class BackCommand implements ICommand {

    public static Map<ServerPlayer, Vec3> backMap = new HashMap<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("back")
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> command) {
        if (command.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
            if (!EssentialsConfig.useBack.get())
                return 0; // Back is disabled

            if (backMap.containsKey(serverPlayer)) {
                Vec3 vec3 = backMap.get(serverPlayer);
                serverPlayer.teleportTo(vec3.x, vec3.y, vec3.z);
                serverPlayer.sendSystemMessage(ChatUtils.getPrefix().plainCopy().append(Component.literal("You have been teleported back to your Death Location!").withStyle(ChatFormatting.GREEN)));
                return 1;
            } else {
                serverPlayer.sendSystemMessage(ChatUtils.getPrefix().plainCopy().append(Component.literal("Your Death Location can't be found!").withStyle(ChatFormatting.RED)));
                return 0;
            }
        }
        return 0;
    }
}
