package org.braekpo1nt.mctmanager.color;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import net.kyori.adventure.text.format.NamedTextColor;

public class ColorMap {

    private static final Map<String, NamedTextColor> COLOR_MAP = new HashMap<>();
    
    static {
        COLOR_MAP.put("black", NamedTextColor.BLACK);
        COLOR_MAP.put("dark_blue", NamedTextColor.DARK_BLUE);
        COLOR_MAP.put("dark_green", NamedTextColor.DARK_GREEN);
        COLOR_MAP.put("dark_aqua", NamedTextColor.DARK_AQUA);
        COLOR_MAP.put("dark_red", NamedTextColor.DARK_RED);
        COLOR_MAP.put("dark_purple", NamedTextColor.DARK_PURPLE);
        COLOR_MAP.put("gold", NamedTextColor.GOLD);
        COLOR_MAP.put("gray", NamedTextColor.GRAY);
        COLOR_MAP.put("dark_gray", NamedTextColor.DARK_GRAY);
        COLOR_MAP.put("blue", NamedTextColor.BLUE);
        COLOR_MAP.put("green", NamedTextColor.GREEN);
        COLOR_MAP.put("aqua", NamedTextColor.AQUA);
        COLOR_MAP.put("red", NamedTextColor.RED);
        COLOR_MAP.put("light_purple", NamedTextColor.LIGHT_PURPLE);
        COLOR_MAP.put("yellow", NamedTextColor.YELLOW);
        COLOR_MAP.put("white", NamedTextColor.WHITE);
    }
    
    public static NamedTextColor getColor(String colorString) {
        NamedTextColor color = COLOR_MAP.get(colorString.toLowerCase());
        return color != null ? color : NamedTextColor.WHITE;
    }
    
    public static boolean hasColor(String colorString) {
        return COLOR_MAP.containsKey(colorString);
    }
    
    public static List<String> getPartiallyMatchingColorStrings(String colorString) {
        return COLOR_MAP.keySet().stream()
                .filter(color -> color.startsWith(colorString))
                .sorted()
                .toList();
    }
}
