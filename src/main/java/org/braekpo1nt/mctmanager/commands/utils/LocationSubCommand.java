package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

class LocationSubCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            sender.sendMessage(Component.text("Usage: /utils location [<player>]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 1) {
            String playerName = args[0];
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                sender.sendMessage(Component.text("Player ")
                                .append(Component.text(playerName)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" not found"))
                        .color(NamedTextColor.RED));
                return true;
            }
            return displayLocation(sender, player);
        }
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can use this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        return displayLocation(sender, player);
    }
    
    private boolean displayLocation(@NotNull CommandSender sender, Player player) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Double.class, new DoubleSerializer())
                .registerTypeAdapter(Float.class, new FloatSerializer())
                .create();
        LocationDTO precise = new LocationDTO(player.getLocation());
        String preciseJson = gson.toJson(precise);
        
        Location blockLocation = player.getLocation().toBlockLocation();
        blockLocation.setYaw((player.getLocation().getYaw()));
        blockLocation.setPitch(player.getLocation().getPitch());
        LocationDTO block = new LocationDTO(blockLocation);
        String blockJson = gson.toJson(block);
        
        LocationDTO rounded = new LocationDTO(new Location(
                player.getLocation().getWorld(),
                roundToNearestHalf(player.getLocation().getX()),
                roundToNearestHalf(player.getLocation().getY()),
                roundToNearestHalf(player.getLocation().getZ()),
                specialRound(player.getLocation().getYaw(), 45),
                specialRound(player.getLocation().getPitch(), 45)
        ));
        String roundedJson = gson.toJson(rounded);
        
        sender.sendMessage(Component.empty()
                .append(attribute("Precise", preciseJson, NamedTextColor.WHITE))
                .append(attribute("Block", blockJson, NamedTextColor.WHITE))
                .append(attribute("Rounded", roundedJson, NamedTextColor.WHITE))
        );
        return false;
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
    
    private double roundToNearestHalf(double value) {
        double floor = Math.floor(value);
        double decimalPart = value - floor;
        
        if (decimalPart < 0.25) {
            return floor;
        } else if (decimalPart >= 0.25 && decimalPart < 0.75) {
            return floor + 0.5;
        } else {
            return floor + 1.0;
        }
    }
    
    /**
     * Rounds a given float value to the closest multiple of the specified increment
     * @param value The float value to be rounded
     * @param increment The increment to which the value should be rounded
     * @return The closest number to the input value that is a multiple of the increment
     */
    private static float specialRound(float value, float increment) {
        float multiple = Math.round(value / increment);
        return multiple * increment;
    }
}
