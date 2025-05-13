package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InvseeCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("invsee")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .executes(this::executeWithContext));
    }

    private int executeWithContext(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "playerName");
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer currentPlayer) {
            MinecraftServer server = source.getServer();
            ServerPlayer targetPlayer = server.getPlayerList().getPlayerByName(playerName);

            if (targetPlayer != null) {
                Component message = ChatUtils.getPrefix().copy().append(
                        Component.literal("Opened " + targetPlayer.getName().getString() + "'s inventory.")
                                .withStyle(style -> style.withColor(ChatFormatting.GREEN))
                );
                currentPlayer.sendSystemMessage(message);
                openPlayerInventory(currentPlayer, targetPlayer);
            } else {
                currentPlayer.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                        Component.literal("Player \"" + playerName + "\" is not online.")
                                .withStyle(style -> style.withColor(ChatFormatting.RED))
                ));
            }
        } else {
            source.sendFailure(ChatUtils.getPrefix().copy().append(
                    Component.literal("You must be a player to use this command.")
                            .withStyle(style -> style.withColor(ChatFormatting.RED))
            ));
        }

        return 1;
    }

    private void openPlayerInventory(ServerPlayer currentPlayer, ServerPlayer targetPlayer) {
        SimpleContainer virtualInventory = new SimpleContainer(36);
        for (int i = 0; i < targetPlayer.getInventory().items.size(); i++) {
            virtualInventory.setItem(i, targetPlayer.getInventory().items.get(i).copy());
        }

        currentPlayer.openMenu(new SimpleMenuProvider(
                (id, playerInventory, playerEntity) ->
                        new VirtualInventoryMenu(id, playerInventory, virtualInventory, targetPlayer),
                Component.literal(targetPlayer.getName().getString() + "'s Inventory")
        ));
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };

    public static class VirtualInventoryMenu extends AbstractContainerMenu {

        private final SimpleContainer virtualInventory;
        private final ServerPlayer targetPlayer;

        public VirtualInventoryMenu(int id, Inventory playerInventory, SimpleContainer virtualInventory, ServerPlayer targetPlayer) {
            super(MenuType.GENERIC_9x4, id);
            this.virtualInventory = virtualInventory;
            this.targetPlayer = targetPlayer;

            // Virtual inventory (9x4)
            for (int row = 0; row < 4; ++row) {
                for (int col = 0; col < 9; ++col) {
                    this.addSlot(new Slot(virtualInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
                }
            }

            // Player inventory slots
            for (int row = 0; row < 3; ++row) {
                for (int col = 0; col < 9; ++col) {
                    this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
                }
            }

            // Hotbar
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
            }
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return true;
        }

        @Override
        public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
            Slot slot = this.slots.get(index);
            if (!slot.hasItem()) return ItemStack.EMPTY;

            ItemStack stack = slot.getItem();
            ItemStack original = stack.copy();

            if (index < this.virtualInventory.getContainerSize()) {
                // Move from virtual inventory to player
                if (!this.moveItemStackTo(stack, this.virtualInventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to virtual
                if (!this.moveItemStackTo(stack, 0, this.virtualInventory.getContainerSize(), false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
            return original;
        }

        public SimpleContainer getVirtualInventory() {
            return virtualInventory;
        }

        public ServerPlayer getTargetPlayer() {
            return targetPlayer;
        }
    }
}