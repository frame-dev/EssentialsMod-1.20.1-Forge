package ch.framedev.essentialsmod.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatUtils {

    public static Component getColoredTextComponent(String message, ChatFormatting chatFormatting) {
        return Component.literal(message).withStyle(chatFormatting);
    }

    public static Component getTextComponent(String[] messages, String[] patternToReplaceColor) {
        if (messages.length != patternToReplaceColor.length) {
            throw new IllegalArgumentException("Pattern array must have the same length as the messages array.");
        }

        MutableComponent combined = Component.literal("");

        for (int i = 0; i < messages.length; i++) {
            ChatFormatting formatting = ChatColor.getByColorCode(patternToReplaceColor[i]);
            if (formatting == null) {
                throw new IllegalArgumentException("Invalid color code: " + patternToReplaceColor[i]);
            }
            combined = combined.append(Component.literal(messages[i]).withStyle(formatting));
            if (i < messages.length - 1) {
                combined = combined.append(Component.literal(" "));
            }
        }

        return combined;
    }

    private static Component getTextComponentForPrefix(String[] messages, String[] patternToReplaceColor) {
        if (messages.length != patternToReplaceColor.length) {
            throw new IllegalArgumentException("Pattern array must have the same length as the messages array.");
        }

        MutableComponent combined = Component.literal("");

        for (int i = 0; i < messages.length; i++) {
            ChatFormatting formatting = ChatColor.getByColorCode(patternToReplaceColor[i]);
            MutableComponent part = Component.literal(messages[i]);
            if (formatting != null) {
                part = part.withStyle(style -> style.withColor(formatting));
            }
            combined = combined.append(part);
        }

        return combined;
    }

    public static Component getPrefix() {
        String[] messages = new String[]{
                "[", "Essentials", "]", " » ", ""
        };
        String[] patternToReplaceColor = new String[]{
                "§a", "§6", "§a", "§c", "§f"
        };
        return getTextComponentForPrefix(messages, patternToReplaceColor);
    }

    public enum ChatColor {
        GRAY(ChatFormatting.GRAY, "§7"),
        RED(ChatFormatting.RED, "§c"),
        BLUE(ChatFormatting.BLUE, "§9"),
        GOLD(ChatFormatting.GOLD, "§6"),
        GREEN(ChatFormatting.GREEN, "§a"),
        YELLOW(ChatFormatting.YELLOW, "§e"),
        AQUA(ChatFormatting.AQUA, "§b"),
        WHITE(ChatFormatting.WHITE, "§f"),
        LIGHT_PURPLE(ChatFormatting.LIGHT_PURPLE, "§d"),
        DARK_PURPLE(ChatFormatting.DARK_PURPLE, "§5"),
        DARK_BLUE(ChatFormatting.DARK_BLUE, "§1"),
        DARK_GREEN(ChatFormatting.DARK_GREEN, "§2"),
        DARK_AQUA(ChatFormatting.DARK_AQUA, "§3"),
        DARK_RED(ChatFormatting.DARK_RED, "§4"),
        DARK_GRAY(ChatFormatting.DARK_GRAY, "§8"),
        BLACK(ChatFormatting.BLACK, "§0"),
        RESET(ChatFormatting.RESET, "§r"),
        ITALIC(ChatFormatting.ITALIC, "§o"),
        BOLD(ChatFormatting.BOLD, "§l"),
        STRIKETHROUGH(ChatFormatting.STRIKETHROUGH, "§m"),
        UNDERLINE(ChatFormatting.UNDERLINE, "§n"),
        OBFUSCATED(ChatFormatting.OBFUSCATED, "§k");

        private final ChatFormatting chatFormatting;
        private final String colorCode;

        ChatColor(ChatFormatting chatFormatting, String colorCode) {
            this.chatFormatting = chatFormatting;
            this.colorCode = colorCode;
        }

        public ChatFormatting getChatFormatting() {
            return chatFormatting;
        }

        public String getColorCode() {
            return colorCode;
        }

        public static ChatFormatting getByColorCode(String colorCode) {
            for (ChatColor chatColor : ChatColor.values()) {
                if (chatColor.getColorCode().equalsIgnoreCase(colorCode)) {
                    return chatColor.getChatFormatting();
                }
            }
            return null;
        }
    }
}