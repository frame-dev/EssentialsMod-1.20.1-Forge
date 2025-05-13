package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.EssentialsMod;
import ch.framedev.essentialsmod.utils.Config;
import ch.framedev.essentialsmod.utils.EssentialsConfig;
import ch.framedev.yamlutils.FileConfiguration;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackpackCommand implements ICommand {

    private static final Map<String, SimpleContainer> backpacks = new HashMap<>();
    private static final Map<String, Boolean> backpackSet = new HashMap<>();
    private static final FileConfiguration config = new Config().getConfig();
    private static final Set<String> openBackpacks = new HashSet<>();

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("backpack")
                .executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can use this command!")
                    .withStyle(style -> style.withColor(ChatFormatting.RED)));
            return 0;
        }

        String uuid = player.getStringUUID();

        if (!backpacks.containsKey(uuid) && EssentialsConfig.enableBackPackSaveInConfig.get()) {
            loadBackpack(uuid);
        }

        SimpleContainer backpack = backpacks.computeIfAbsent(uuid, k -> new SimpleContainer(27));

        player.openMenu(new SimpleMenuProvider(
                (id, playerInventory, entity) -> new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, backpack, 3),
                Component.literal(player.getName().getString() + "'s Backpack")
        ));
        backpackSet.put(uuid, true);
        openBackpacks.add(uuid); // Track opened backpack

        return 1;
    }

    private static void loadBackpack(String uuid) {
        if (config.containsKey(uuid)) {
            SimpleContainer backpack = new SimpleContainer(27);
            for (int i = 0; i < 27; i++) {
                String key = uuid + ".slot" + i;
                if (config.containsKey(key)) {
                    String nbtString = String.valueOf(config.get(key));
                    try {
                        ItemStack item = InventoryUtils.deserializeItemStack(nbtString);
                        backpack.setItem(i, item);
                    } catch (IOException | CommandSyntaxException e) {
                        EssentialsMod.getLOGGER().warn("Failed to load item in backpack slot {}: {}", i, e.getMessage());
                    }
                }
            }
            backpacks.put(uuid, backpack);
        }
    }

    private static void saveBackpack(String uuid) {
        if (!backpacks.containsKey(uuid)) return;

        SimpleContainer backpack = backpacks.get(uuid);
        for (int i = 0; i < backpack.getContainerSize(); i++) {
            ItemStack item = backpack.getItem(i);
            String nbtString = InventoryUtils.serializeItemStack(item);
            config.set(uuid + ".slot" + i, nbtString);
        }
        config.save();
        EssentialsMod.getLOGGER().info("Backpack saved for UUID {}", uuid);
    }

    @Mod.EventBusSubscriber(modid = "essentials")
    public static class BackpackEventHandler {

        @SubscribeEvent
        public static void onContainerClose(PlayerContainerEvent.Close event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                String uuid = player.getStringUUID();

                if (backpacks.containsKey(uuid)
                    && EssentialsConfig.enableBackPackSaveInConfig.get()
                    && backpackSet.containsKey(uuid)
                    && openBackpacks.contains(uuid)) {

                    saveBackpack(uuid);
                    backpackSet.remove(uuid);
                    openBackpacks.remove(uuid); // Clean up after save
                }
            }
        }
    }

    public static class InventoryUtils {

        public static String serializeItemStack(ItemStack item) {
            CompoundTag tag = new CompoundTag();
            item.save(tag);
            return tag.toString();
        }

        public static ItemStack deserializeItemStack(String nbtString) throws IOException, CommandSyntaxException {
            CompoundTag tag = TagParser.parseTag(nbtString);
            return ItemStack.of(tag);
        }
    }
}