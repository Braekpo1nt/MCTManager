package org.braekpo1nt.mctmanager.color;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;

public class ColorMap {

    private static final Map<String, NamedTextColor> NAMED_TEXT_COLOR_MAP = new HashMap<>();
    static {
        NAMED_TEXT_COLOR_MAP.put("aqua", NamedTextColor.AQUA);
        NAMED_TEXT_COLOR_MAP.put("black", NamedTextColor.BLACK);
        NAMED_TEXT_COLOR_MAP.put("blue", NamedTextColor.BLUE);
        NAMED_TEXT_COLOR_MAP.put("dark_aqua", NamedTextColor.DARK_AQUA);
        NAMED_TEXT_COLOR_MAP.put("dark_blue", NamedTextColor.DARK_BLUE);
        NAMED_TEXT_COLOR_MAP.put("dark_gray", NamedTextColor.DARK_GRAY);
        NAMED_TEXT_COLOR_MAP.put("dark_green", NamedTextColor.DARK_GREEN);
        NAMED_TEXT_COLOR_MAP.put("dark_purple", NamedTextColor.DARK_PURPLE);
        NAMED_TEXT_COLOR_MAP.put("dark_red", NamedTextColor.DARK_RED);
        NAMED_TEXT_COLOR_MAP.put("gold", NamedTextColor.GOLD);
        NAMED_TEXT_COLOR_MAP.put("gray", NamedTextColor.GRAY);
        NAMED_TEXT_COLOR_MAP.put("green", NamedTextColor.GREEN);
        NAMED_TEXT_COLOR_MAP.put("light_purple", NamedTextColor.LIGHT_PURPLE);
        NAMED_TEXT_COLOR_MAP.put("red", NamedTextColor.RED);
        NAMED_TEXT_COLOR_MAP.put("white", NamedTextColor.WHITE);
        NAMED_TEXT_COLOR_MAP.put("yellow", NamedTextColor.YELLOW);
    }
    
    private static final Map<String, Color> COLOR_MAP = new HashMap<>();
    
    static {
        for (Map.Entry<String, NamedTextColor> entry : NAMED_TEXT_COLOR_MAP.entrySet()) {
            NamedTextColor namedTextColor = entry.getValue();
            Color bukkitColor = Color.fromBGR(namedTextColor.blue(), namedTextColor.green(), namedTextColor.red());
            COLOR_MAP.put(entry.getKey(), bukkitColor);
        }
    }
    
    private static final Map<String, ChatColor> CHAT_COLOR_MAP = new HashMap<>();
    
    static {
        CHAT_COLOR_MAP.put("aqua", ChatColor.AQUA);
        CHAT_COLOR_MAP.put("black", ChatColor.BLACK);
        CHAT_COLOR_MAP.put("blue", ChatColor.BLUE);
        CHAT_COLOR_MAP.put("dark_aqua", ChatColor.DARK_AQUA);
        CHAT_COLOR_MAP.put("dark_blue", ChatColor.DARK_BLUE);
        CHAT_COLOR_MAP.put("dark_gray", ChatColor.DARK_GRAY);
        CHAT_COLOR_MAP.put("dark_green", ChatColor.DARK_GREEN);
        CHAT_COLOR_MAP.put("dark_purple", ChatColor.DARK_PURPLE);
        CHAT_COLOR_MAP.put("dark_red", ChatColor.DARK_RED);
        CHAT_COLOR_MAP.put("gold", ChatColor.GOLD);
        CHAT_COLOR_MAP.put("gray", ChatColor.GRAY);
        CHAT_COLOR_MAP.put("green", ChatColor.GREEN);
        CHAT_COLOR_MAP.put("light_purple", ChatColor.LIGHT_PURPLE);
        CHAT_COLOR_MAP.put("red", ChatColor.RED);
        CHAT_COLOR_MAP.put("white", ChatColor.WHITE);
        CHAT_COLOR_MAP.put("yellow", ChatColor.YELLOW);
    }
    
    public static NamedTextColor getNamedTextColor(String colorString) {
        NamedTextColor color = NAMED_TEXT_COLOR_MAP.get(colorString.toLowerCase());
        return color != null ? color : NamedTextColor.WHITE;
    }
    
    public static Color getColor(String colorString) {
        Color color = COLOR_MAP.get(colorString);
        return color != null ? color : Color.WHITE;
    }
    
    public static boolean hasNamedTextColor(String colorString) {
        return NAMED_TEXT_COLOR_MAP.containsKey(colorString);
    }
    
    public static List<String> getPartiallyMatchingColorStrings(String colorString) {
        return NAMED_TEXT_COLOR_MAP.keySet().stream()
                .filter(color -> color.startsWith(colorString))
                .sorted()
                .toList();
    }
    
    public static ChatColor getChatColor(String colorString) {
        ChatColor color = CHAT_COLOR_MAP.get(colorString.toLowerCase());
        return color != null ? color : ChatColor.WHITE;
    }
}
