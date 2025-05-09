package org.braekpo1nt.mctmanager.utils;

import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.ColorAttributes;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    private static final Map<String, Material> POWDER_TYPE_MAP = new HashMap<>();
    static {
        POWDER_TYPE_MAP.put("aqua", Material.LIGHT_BLUE_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("black", Material.BLACK_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("dark_aqua", Material.CYAN_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("dark_blue", Material.BLUE_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("dark_gray", Material.GRAY_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("dark_green", Material.GREEN_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("dark_purple", Material.PURPLE_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("dark_red", Material.RED_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("gold", Material.ORANGE_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("gray", Material.LIGHT_GRAY_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("green", Material.LIME_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("light_purple", Material.MAGENTA_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("red", Material.RED_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("white", Material.WHITE_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("blue", Material.BLUE_CONCRETE_POWDER);
        POWDER_TYPE_MAP.put("yellow", Material.YELLOW_CONCRETE_POWDER);
    }
    
    private static final Map<String, Material> CONCRETE_TYPE_MAP = new HashMap<>();
    static {
        CONCRETE_TYPE_MAP.put("aqua", Material.LIGHT_BLUE_CONCRETE);
        CONCRETE_TYPE_MAP.put("black", Material.BLACK_CONCRETE);
        CONCRETE_TYPE_MAP.put("dark_aqua", Material.CYAN_CONCRETE);
        CONCRETE_TYPE_MAP.put("dark_blue", Material.BLUE_CONCRETE);
        CONCRETE_TYPE_MAP.put("dark_gray", Material.GRAY_CONCRETE);
        CONCRETE_TYPE_MAP.put("dark_green", Material.GREEN_CONCRETE);
        CONCRETE_TYPE_MAP.put("dark_purple", Material.PURPLE_CONCRETE);
        CONCRETE_TYPE_MAP.put("dark_red", Material.RED_CONCRETE);
        CONCRETE_TYPE_MAP.put("gold", Material.ORANGE_CONCRETE);
        CONCRETE_TYPE_MAP.put("gray", Material.LIGHT_GRAY_CONCRETE);
        CONCRETE_TYPE_MAP.put("green", Material.LIME_CONCRETE);
        CONCRETE_TYPE_MAP.put("light_purple", Material.MAGENTA_CONCRETE);
        CONCRETE_TYPE_MAP.put("red", Material.RED_CONCRETE);
        CONCRETE_TYPE_MAP.put("white", Material.WHITE_CONCRETE);
        CONCRETE_TYPE_MAP.put("blue", Material.BLUE_CONCRETE);
        CONCRETE_TYPE_MAP.put("yellow", Material.YELLOW_CONCRETE);
    }
    
    private static final Map<String, Material> STAINED_GLASS_MAP = new HashMap<>();
    static {
        STAINED_GLASS_MAP.put("aqua", Material.LIGHT_BLUE_STAINED_GLASS);
        STAINED_GLASS_MAP.put("black", Material.BLACK_STAINED_GLASS);
        STAINED_GLASS_MAP.put("dark_aqua", Material.CYAN_STAINED_GLASS);
        STAINED_GLASS_MAP.put("dark_blue", Material.BLUE_STAINED_GLASS);
        STAINED_GLASS_MAP.put("dark_gray", Material.GRAY_STAINED_GLASS);
        STAINED_GLASS_MAP.put("dark_green", Material.GREEN_STAINED_GLASS);
        STAINED_GLASS_MAP.put("dark_purple", Material.PURPLE_STAINED_GLASS);
        STAINED_GLASS_MAP.put("dark_red", Material.RED_STAINED_GLASS);
        STAINED_GLASS_MAP.put("gold", Material.ORANGE_STAINED_GLASS);
        STAINED_GLASS_MAP.put("gray", Material.LIGHT_GRAY_STAINED_GLASS);
        STAINED_GLASS_MAP.put("green", Material.LIME_STAINED_GLASS);
        STAINED_GLASS_MAP.put("light_purple", Material.MAGENTA_STAINED_GLASS);
        STAINED_GLASS_MAP.put("red", Material.RED_STAINED_GLASS);
        STAINED_GLASS_MAP.put("white", Material.WHITE_STAINED_GLASS);
        STAINED_GLASS_MAP.put("blue", Material.BLUE_STAINED_GLASS);
        STAINED_GLASS_MAP.put("yellow", Material.YELLOW_STAINED_GLASS);
    }
    
    private static final Map<String, Material> STAINED_GLASS_PANE_MAP = new HashMap<>();
    static {
        STAINED_GLASS_PANE_MAP.put("aqua", Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("black", Material.BLACK_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("dark_aqua", Material.CYAN_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("dark_blue", Material.BLUE_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("dark_gray", Material.GRAY_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("dark_green", Material.GREEN_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("dark_purple", Material.PURPLE_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("dark_red", Material.RED_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("gold", Material.ORANGE_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("gray", Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("green", Material.LIME_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("light_purple", Material.MAGENTA_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("red", Material.RED_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("white", Material.WHITE_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("blue", Material.BLUE_STAINED_GLASS_PANE);
        STAINED_GLASS_PANE_MAP.put("yellow", Material.YELLOW_STAINED_GLASS_PANE);
    }
    
    private static final Map<String, Material> WOOL_MAP = new HashMap<>();
    static {
        WOOL_MAP.put("aqua", Material.LIGHT_BLUE_WOOL);
        WOOL_MAP.put("black", Material.BLACK_WOOL);
        WOOL_MAP.put("dark_aqua", Material.CYAN_WOOL);
        WOOL_MAP.put("dark_blue", Material.BLUE_WOOL);
        WOOL_MAP.put("dark_gray", Material.GRAY_WOOL);
        WOOL_MAP.put("dark_green", Material.GREEN_WOOL);
        WOOL_MAP.put("dark_purple", Material.PURPLE_WOOL);
        WOOL_MAP.put("dark_red", Material.RED_WOOL);
        WOOL_MAP.put("gold", Material.ORANGE_WOOL);
        WOOL_MAP.put("gray", Material.LIGHT_GRAY_WOOL);
        WOOL_MAP.put("green", Material.LIME_WOOL);
        WOOL_MAP.put("light_purple", Material.MAGENTA_WOOL);
        WOOL_MAP.put("red", Material.RED_WOOL);
        WOOL_MAP.put("white", Material.WHITE_WOOL);
        WOOL_MAP.put("blue", Material.BLUE_WOOL);
        WOOL_MAP.put("yellow", Material.YELLOW_WOOL);
    }
    
    private static final Map<String, Material> BANNER_TYPE_MAP = new HashMap<>();
    static {
        BANNER_TYPE_MAP.put("aqua", Material.LIGHT_BLUE_BANNER);
        BANNER_TYPE_MAP.put("black", Material.BLACK_BANNER);
        BANNER_TYPE_MAP.put("dark_aqua", Material.CYAN_BANNER);
        BANNER_TYPE_MAP.put("dark_blue", Material.BLUE_BANNER);
        BANNER_TYPE_MAP.put("dark_gray", Material.GRAY_BANNER);
        BANNER_TYPE_MAP.put("dark_green", Material.GREEN_BANNER);
        BANNER_TYPE_MAP.put("dark_purple", Material.PURPLE_BANNER);
        BANNER_TYPE_MAP.put("dark_red", Material.RED_BANNER);
        BANNER_TYPE_MAP.put("gold", Material.ORANGE_BANNER);
        BANNER_TYPE_MAP.put("gray", Material.LIGHT_GRAY_BANNER);
        BANNER_TYPE_MAP.put("green", Material.LIME_BANNER);
        BANNER_TYPE_MAP.put("light_purple", Material.MAGENTA_BANNER);
        BANNER_TYPE_MAP.put("red", Material.RED_BANNER);
        BANNER_TYPE_MAP.put("white", Material.WHITE_BANNER);
        BANNER_TYPE_MAP.put("blue", Material.BLUE_BANNER);
        BANNER_TYPE_MAP.put("yellow", Material.YELLOW_BANNER);
    }
    
    public static @NotNull NamedTextColor getNamedTextColor(String colorString) {
        return NAMED_TEXT_COLOR_MAP.getOrDefault(colorString.toLowerCase(), NamedTextColor.WHITE);
    }
    
    public static Color getColor(String colorString) {
        return COLOR_MAP.getOrDefault(colorString, Color.WHITE);
    }
    
    public static boolean hasNamedTextColor(String colorString) {
        return NAMED_TEXT_COLOR_MAP.containsKey(colorString);
    }
    
    public static Collection<NamedTextColor> getNamedTextColors() {
        return NAMED_TEXT_COLOR_MAP.values();
    }
    
    public static List<String> getPartiallyMatchingColorStrings(String colorString) {
        return NAMED_TEXT_COLOR_MAP.keySet().stream()
                .filter(color -> color.startsWith(colorString))
                .sorted()
                .toList();
    }
    
    public static List<Material> getAllConcretePowderColors() {
        return POWDER_TYPE_MAP.values().stream().toList();
    }
    
    /**
     * Returns the type of concrete powder which most closely matches the given colorString.
     * Note that some color strings are duplicates.
     * @param colorString The color string to get the concrete powder for. Should be the color strings matching
     *                    the ChatColor values.
     * @return The concrete powder color that best matches the given color string. White if unrecognized.
     */
    public static Material getConcretePowderColor(String colorString) {
        return POWDER_TYPE_MAP.getOrDefault(colorString, Material.WHITE_CONCRETE_POWDER);
    }
    
    /**
     * Returns the type of concrete which most closely matches the given colorString.
     * Note that some color strings are duplicates.
     * @param colorString The color string to get the concrete for. Should be the color strings matching
     *                    the ChatColor values.
     * @return The concrete color that best matches the given color string. White if unrecognized.
     */
    public static Material getConcreteColor(String colorString) {
        return CONCRETE_TYPE_MAP.getOrDefault(colorString, Material.WHITE_CONCRETE);
    }
    
    /**
     * Gets the color of banner associated with the given color string. Note that some are duplicates.
     * @param colorString The color string 
     * @return The banner color type most closely matching the given color string. White if unrecognized. 
     */
    public static Material getBannerColor(String colorString) {
        return BANNER_TYPE_MAP.getOrDefault(colorString, Material.WHITE_BANNER);
    }
    
    /**
     * Gets the color of stained-glass associated with the given color string. 
     * @param colorString the color string to get the stained-glass for. Should be the color string matching the ChatColor values.
     * @return The stained-glass color that best matches the given color string. White if unrecognized.
     */
    public static Material getStainedGlassColor(String colorString) {
        return STAINED_GLASS_MAP.getOrDefault(colorString, Material.WHITE_STAINED_GLASS);
    }
    
    /**
     * Gets the color of stained-glass-pane associated with the given color string. 
     * @param colorString the color string to get the stained-glass-pane for. Should be the color string matching the ChatColor values.
     * @return The stained-glass-pane color that best matches the given color string. White if unrecognized.
     */
    public static Material getStainedGlassPaneColor(String colorString) {
        return STAINED_GLASS_PANE_MAP.getOrDefault(colorString, Material.WHITE_STAINED_GLASS_PANE);
    }
    
    /**
     * Gets the color of wool associated with the given color string. 
     * @param colorString the color string to get the wool for. Should be the color string matching the ChatColor values.
     * @return The wool color that best matches the given color string. White if unrecognized.
     */
    public static Material getWoolColor(String colorString) {
        return WOOL_MAP.getOrDefault(colorString, Material.WHITE_WOOL);
    }
    
    /**
     * @param colorString the color string indicating which colors the attributes should use. Should be the color string matching the ChatColor values.
     * @return the {@link ColorAttributes} associated with the given colorString
     */
    public static ColorAttributes getColorAttributes(String colorString) {
        return new ColorAttributes(
                getConcretePowderColor(colorString),
                getConcreteColor(colorString),
                getStainedGlassColor(colorString),
                getStainedGlassPaneColor(colorString),
                getBannerColor(colorString),
                getWoolColor(colorString)
        );
    }
    
    /**
     * Colors all leather armor in the equipment slots of the participant to be the team color
     * If the {@link org.bukkit.persistence.PersistentDataContainer} of an item's LeatherArmorMeta contains the {@link GameManagerUtils#IGNORE_TEAM_COLOR} {@link PersistentDataType#STRING} property, then that item will not be colored. 
     * @param participant the participant whose armor slots may or may not contain leather armor, but for whom any existing leather armor slots should be colored their team color. If this participant is not a participiant in the given gameManager, then nothing happens. 
     */
    public static void colorLeatherArmor(@NotNull Participant participant, @NotNull Color teamColor) {
        GameManagerUtils.colorLeatherArmor(participant.getInventory().getHelmet(), teamColor);
        GameManagerUtils.colorLeatherArmor(participant.getInventory().getChestplate(), teamColor);
        GameManagerUtils.colorLeatherArmor(participant.getInventory().getLeggings(), teamColor);
        GameManagerUtils.colorLeatherArmor(participant.getInventory().getBoots(), teamColor);
    }
}
