package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
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
    
    public static final Set<Material> TRANSPARENT = Set.of(Material.AIR, Material.WATER, Material.LAVA);
    
    public static Component attribute(String title, double value, NamedTextColor color) {
        String valueStr = value == Math.floor(value) ? ""+(int)value : ""+value;
        return attribute(title, valueStr, color);
    }
    
    public static Component attribute(String title, String value, NamedTextColor color) {
        return Component.empty()
                .append(Component.text("\n"))
                .append(Component.text(title))
                .append(Component.text(": "))
                .append(Component.text(value)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(""+value)))
                .color(color);
    }
    
    // TODO: make this javadoc more accurately describe what this is doing, and also make it more agnostic (such that it can return any number of argsLengths
    /**
     * Helper method for tab-completing coordinate values for commands.
     * @param player This player will be used to tab-complete coordinates which they are looking at or where they are standing
     * @param argsLength number of the argument to provide. Values outside the range [1,6] will return {@link Collections#emptyList()}. The position of the arg will determine which coordinate value to return. 
     * @return The coordinate element of the block the player is looking at, or, if the player is not looking at a non-transparent block in a 5-block distance, the coordinate element of the player's position.
     */
    public static @NotNull List<@NotNull String> tabCompleteCoordinates(@NotNull Player player, int argsLength) {
        if (argsLength == 0 || argsLength > 6) {
            return Collections.emptyList();
        }
        Block targetBlock = player.getTargetBlock(TRANSPARENT, 5);
        if (TRANSPARENT.contains(targetBlock.getType())) {
            switch (argsLength) {
                case 1, 4 -> {
                    return Collections.singletonList("" + player.getLocation().getBlockX());
                }
                case 2, 5 -> {
                    return Collections.singletonList("" + player.getLocation().getBlockY());
                }
                case 3, 6 -> {
                    return Collections.singletonList("" + player.getLocation().getBlockZ());
                }
            }
        }
        switch (argsLength) {
            case 1, 4 -> {
                return Collections.singletonList("" + targetBlock.getLocation().getBlockX());
            }
            case 2, 5 -> {
                return Collections.singletonList("" + targetBlock.getLocation().getBlockY());
            }
            case 3, 6 -> {
                return Collections.singletonList("" + targetBlock.getLocation().getBlockZ());
            }
        }
        return Collections.emptyList();
    }
}
