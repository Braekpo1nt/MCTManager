package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.Set;

abstract class UtilsUtils {
    
    /**
     * 
     */
    static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Double.class, new DoubleSerializer())
            .registerTypeAdapter(Float.class, new FloatSerializer())
            .create();
    
    static final Set<Material> TRANSPARENT = Set.of(Material.AIR, Material.WATER, Material.LAVA);
    
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
