package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class DistSubCommand implements TabExecutor {
    
    private final Set<Material> transparent = Set.of(Material.AIR, Material.WATER, Material.LAVA);
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 6) {
            sender.sendMessage(Component.text("Usage: /utils dist <x1> <y1> <z1> <x2> <y2> <z2>")
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
        
        Vector vector1 = new Vector(x1, y1, z1);
        Vector vector2 = new Vector(x2, y2, z2);
        double distance = vector1.distance(vector2);
        String vector1Str = String.format("%s, %s, %s", x1, y1, z1);
        String vector2Str = String.format("%s, %s, %s", x2, y2, z2);
        sender.sendMessage(Component.empty()
                .append(Component.text("Distance from ("))
                .append(Component.text(vector1Str)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(vector1Str)))
                .append(Component.text(") to ("))
                .append(Component.text(vector2Str)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(vector2Str)))
                .append(Component.text(") is "))
                .append(Component.text(distance)
                        .decorate(TextDecoration.BOLD)
                        .hoverEvent(HoverEvent.showText(Component.text("Copy to clipboard")))
                        .clickEvent(ClickEvent.copyToClipboard(String.format("%s", distance))))
        );
        return true;
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
