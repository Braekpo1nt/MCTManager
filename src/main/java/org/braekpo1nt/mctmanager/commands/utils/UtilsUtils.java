package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.Set;

abstract class UtilsUtils {
    
    static final Set<Material> TRANSPARENT = Set.of(Material.AIR, Material.WATER, Material.LAVA);
    
    /**
     * Rounds a given float value to the closest multiple of the specified increment
     * @param value The float value to be rounded
     * @param increment The increment to which the value should be rounded
     * @return The closest number to the input value that is a multiple of the increment
     */
    public static float specialRound(float value, float increment) {
        float multiple = Math.round(value / increment);
        return multiple * increment;
    }
    
    /**
     * Rounds a given double value to the closest multiple of the specified increment
     * @param value The double value to be rounded
     * @param increment The increment to which the value should be rounded
     * @return The closest number to the input value that is a multiple of the increment
     */
    public static double specialRound(double value, double increment) {
        float multiple = Math.round(value / increment);
        return multiple * increment;
    }
    
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
    
}
