package ch.framedev.essentialsmod.commands;

import ch.framedev.essentialsmod.utils.ChatUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.UUID;

import static net.minecraft.world.entity.EquipmentSlot.MAINHAND;

public class AdminSwordCommand implements ICommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("adminsword")
                .requires(source -> source.hasPermission(3))
                .then(Commands.argument("playerName", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTION)
                        .then(Commands.argument("looting", IntegerArgumentType.integer())
                                .executes(this::executeWithPlayerAndLooting))
                        .executes(this::executeWithPlayerOnly))
                .then(Commands.argument("looting", IntegerArgumentType.integer())
                        .executes(this::executeWithLootingOnly))
                .executes(this::executeSelf);
    }

    private int executeSelf(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            giveAdminSword(player, false);
            context.getSource().sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("You successfully got the Admin Sword!").withStyle(ChatFormatting.GREEN)));
            return 1;
        }
        return 0;
    }

    private int executeWithLootingOnly(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            boolean looting = IntegerArgumentType.getInteger(context, "looting") == 1;
            giveAdminSword(player, looting);
            context.getSource().sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("You successfully got the Admin Sword!").withStyle(ChatFormatting.GREEN)));
            return 1;
        }
        return 0;
    }

    private int executeWithPlayerOnly(CommandContext<CommandSourceStack> context) {
        String targetName = StringArgumentType.getString(context, "playerName");
        ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayerByName(targetName);

        if (target != null) {
            giveAdminSword(target, false);
            target.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("You successfully got the Admin Sword!").withStyle(ChatFormatting.GREEN)));
            context.getSource().sendSystemMessage(Component.literal(targetName + " has received the Admin Sword."));
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Player not found: " + targetName).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private int executeWithPlayerAndLooting(CommandContext<CommandSourceStack> context) {
        String targetName = StringArgumentType.getString(context, "playerName");
        boolean looting = IntegerArgumentType.getInteger(context, "looting") == 1;
        ServerPlayer target = context.getSource().getServer().getPlayerList().getPlayerByName(targetName);

        if (target != null) {
            giveAdminSword(target, looting);
            target.sendSystemMessage(ChatUtils.getPrefix().copy().append(
                    Component.literal("You successfully got the Admin Sword!").withStyle(ChatFormatting.GREEN)));
            context.getSource().sendSystemMessage(Component.literal(targetName + " has received the Admin Sword."));
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Player not found: " + targetName).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private void giveAdminSword(Player player, boolean looting) {
        ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "Attack Damage Modifier", 300, AttributeModifier.Operation.ADDITION);
        sword.addAttributeModifier(Attributes.ATTACK_DAMAGE, modifier, MAINHAND);
        sword.enchant(Enchantments.SHARPNESS, 126);
        if (looting) {
            sword.enchant(Enchantments.MOB_LOOTING, 100);
        }
        player.getInventory().add(sword);
    }

    private final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getGameProfile().getName());
        }
        return builder.buildFuture();
    };
}