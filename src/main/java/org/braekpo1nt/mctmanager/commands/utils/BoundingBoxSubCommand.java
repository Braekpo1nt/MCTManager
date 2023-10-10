package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class BoundingBoxSubCommand implements TabExecutor {
    
    private final Set<Material> transparent = Set.of(Material.AIR, Material.WATER, Material.LAVA);
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 6) {
            sender.sendMessage(Component.text("Usage: /utils boundingbox <x1> <y1> <z1> <x2> <y2> <z2>")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        for (String coordinate : args) {
            if (!CommandUtils.isDouble(coordinate)) {
                sender.sendMessage(Component.text(coordinate)
                        .append(Component.text(" is not a number"))
                        .color(NamedTextColor.RED));
                return true;
            }
        }
        
        double x1 = Double.parseDouble(args[0]);
        double y1 = Double.parseDouble(args[1]);
        double z1 = Double.parseDouble(args[2]);
        double x2 = Double.parseDouble(args[3]);
        double y2 = Double.parseDouble(args[4]);
        double z2 = Double.parseDouble(args[5]);
        
        BoundingBox boundingBox = new BoundingBox(x1, y1, z1, x2, y2, z2);
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Double.class, new DoubleSerializer())
                .create();
        String json = gson.toJson(boundingBox);
        sender.sendMessage(Component.empty()
                .append(Component.text("BoundingBox:\n"))
                .append(Component.text(json)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(json)))
                .append(attribute("Height", boundingBox.getHeight(), NamedTextColor.GREEN))
                .append(attribute("WidthX", boundingBox.getWidthX(), NamedTextColor.RED))
                .append(attribute("WidthZ", boundingBox.getWidthZ(), NamedTextColor.BLUE))
                .append(attribute("Volume", boundingBox.getVolume(), NamedTextColor.WHITE))
                .append(attribute("Center", String.format("%s, %s, %s", boundingBox.getCenterX(), boundingBox.getCenterY(), boundingBox.getCenterZ()), NamedTextColor.WHITE))
        );
        return true;
    }
    
    
    private Component attribute(String title, double value, NamedTextColor color) {
        String valueStr = value == Math.floor(value) ? ""+(int)value : ""+value;
        return attribute(title, valueStr, color);
    }
    
    private Component attribute(String title, String value, NamedTextColor color) {
        return Component.empty()
                .append(Component.text("\n"))
                .append(Component.text(title))
                .append(Component.text(": "))
                .append(Component.text(value)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(""+value)))
                .color(color);
    }
    
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args.length > 6) {
            return Collections.emptyList();
        }
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        Block targetBlock = player.getTargetBlock(transparent, 5);
        if (transparent.contains(targetBlock.getType())) {
            return Collections.emptyList();
        }
        switch (args.length) {
            case 1, 4 -> {
                return Collections.singletonList(""+targetBlock.getLocation().getBlockX());
            }
            case 2, 5 -> {
                return Collections.singletonList(""+targetBlock.getLocation().getBlockY());
            }
            case 3, 6 -> {
                return Collections.singletonList(""+targetBlock.getLocation().getBlockZ());
            }
        }
        return Collections.emptyList();
    }
}
