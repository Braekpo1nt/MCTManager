package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class UtilsUtils {
    
    private UtilsUtils() {
        // do not instantiate
    }
    
    /**
     *
     */
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Double.class, new DoubleSerializer())
            .registerTypeAdapter(Float.class, new FloatSerializer())
            .create();
    
    public static final Set<Material> TRANSPARENT = Set.of(Material.AIR, Material.WATER, Material.LAVA, Material.LIGHT, Material.VOID_AIR, Material.STRUCTURE_VOID, Material.BARRIER);
    
    public static Component attribute(String title, double value, NamedTextColor color) {
        String valueStr = value == Math.floor(value) ? "" + (int) value : "" + value;
        return attribute(title, valueStr, color);
    }
    
    public static Component attribute(String title, String value, NamedTextColor color) {
        return Component.empty()
                .append(Component.text("\n"))
                .append(Component.text(title))
                .append(Component.text(": "))
                .append(Component.text(value)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard("" + value)))
                .color(color);
    }
    
    /**
     * Takes in an argument position and returns a coordinate value for tab completion.
     * The coordinate value returned is either the x, y, or z component of either the player's location or the block
     * they are looking at.
     * Whether it is the x, y, or z component is determined by the value of {@code `(argPos % 3)`} (0 results in x; 1
     * results in y; 2 results in z).
     * Whether it is from the player's location or the block they're targeting is determined by the distance between the
     * player and the block they're looking at, as well as the material type of that block. If the player is looking at
     * a material which is not in {@link UtilsUtils#TRANSPARENT} and is at most 5 blocks away, then the block position
     * will be used. Otherwise, the player's location will be usd.
     * @param player the player to use as the location or targeted block location
     * @param argPos the position of the argument to tab complete (determines which component of the position to return,
     * either x, y, or z)
     * @return the appropriate coordinate component for the block the player is targeting or the player's location,
     * depending on distance of and transparency of the targeted block. The "block" value will always be used (e.g.
     * {@link Location#getBlockX()}).
     */
    public static @NotNull List<@NotNull String> tabCompleteCoordinates(@NotNull Player player, int argPos) {
        int m = (argPos % 3);
        Block targetBlock = player.getTargetBlock(TRANSPARENT, 5);
        Location location;
        if (TRANSPARENT.contains(targetBlock.getType())) {
            location = player.getLocation();
        } else {
            location = targetBlock.getLocation();
        }
        switch (m) {
            case 1 -> {
                return Collections.singletonList("" + location.getBlockX());
            }
            case 2 -> {
                return Collections.singletonList("" + location.getBlockY());
            }
            case 0 -> {
                return Collections.singletonList("" + location.getBlockZ());
            }
        }
        return Collections.emptyList();
    }
}
