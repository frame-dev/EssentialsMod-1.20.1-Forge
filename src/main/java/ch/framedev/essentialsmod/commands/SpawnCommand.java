package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import ch.framedev.essentialsmod.utils.Config;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public class SpawnCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("spawn")
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) return 0;

        ServerLevel currentLevel = context.getSource().getLevel();

        try {
            Config config = new Config();

            if (!config.containsKey("spawn.dimension")) {
                return handleFallback(player, config, currentLevel);
            }

            // Attempt dimension teleport
            String dimension = config.getString("spawn.dimension");
            int x = config.getConfig().getInt("spawn.x");
            int y = config.getConfig().getInt("spawn.y");
            int z = config.getConfig().getInt("spawn.z");

            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimension));
            ServerLevel targetLevel = player.getServer().getLevel(dimensionKey);

            if (targetLevel != null) {
                player.teleportTo(targetLevel, x + 0.5, y, z + 0.5, 0f, 0f);
                player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                        Component.literal("Teleported to configured spawn point.")
                                .withStyle(style -> style.withColor(ChatFormatting.GREEN))
                ));
                return 1;
            } else {
                // Invalid dimension
                player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                        Component.literal("Invalid dimension \"" + dimension + "\". Falling back to shared spawn.")
                                .withStyle(style -> style.withColor(ChatFormatting.RED))
                ));
                return handleFallback(player, config, currentLevel);
            }

        } catch (Exception e) {
            player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("An error occurred. Teleporting to shared spawn.")
                            .withStyle(style -> style.withColor(ChatFormatting.RED))
            ));
            return handleFallback(player, new Config(), currentLevel);
        }
    }

    private int handleFallback(ServerPlayer player, Config config, ServerLevel level) {
        if (!config.getConfig().containsKey("spawn.x")) {
            BlockPos defaultSpawn = level.getSharedSpawnPos();
            BlockPos safe = findSafeSpawn(level, defaultSpawn);

            if (safe != null) {
                player.teleportTo(safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5);
                player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                        Component.literal("Teleported to the world's safe spawn point.")
                                .withStyle(style -> style.withColor(ChatFormatting.YELLOW))
                ));
                return 1;
            } else {
                player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                        Component.literal("Unable to find a safe spawn location.")
                                .withStyle(style -> style.withColor(ChatFormatting.RED))
                ));
                return 0;
            }
        } else {
            int x = config.getConfig().getInt("spawn.x");
            int y = config.getConfig().getInt("spawn.y");
            int z = config.getConfig().getInt("spawn.z");

            player.teleportTo(x + 0.5, y, z + 0.5);
            player.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("Teleported to configured fallback coordinates in the Overworld.")
                            .withStyle(style -> style.withColor(ChatFormatting.GREEN))
            ));
            return 1;
        }
    }

    private BlockPos findSafeSpawn(LevelAccessor world, BlockPos pos) {
        int safeY = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        BlockPos safe = new BlockPos(pos.getX(), safeY, pos.getZ());

        if (world.getBlockState(safe.below()).isSolidRender(world, safe.below())
            && world.getBlockState(safe).isAir()
            && world.getBlockState(safe.above()).isAir()) {
            return safe;
        }

        return null;
    }
}