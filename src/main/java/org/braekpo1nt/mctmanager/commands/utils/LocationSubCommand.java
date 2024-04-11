package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.braekpo1nt.mctmanager.utils.MathUtils;
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
        LocationDTO precise = new LocationDTO(player.getLocation());
        String preciseJson = UtilsUtils.GSON.toJson(precise);
        
        Location blockLocation = player.getLocation().toBlockLocation();
        blockLocation.setYaw((player.getLocation().getYaw()));
        blockLocation.setPitch(player.getLocation().getPitch());
        LocationDTO block = new LocationDTO(blockLocation);
        String blockJson = UtilsUtils.GSON.toJson(block);
        
        LocationDTO rounded = new LocationDTO(
                MathUtils.specialRound(player.getLocation(), 0.5, 45)
        );
        String roundedJson = UtilsUtils.GSON.toJson(rounded);
        
        sender.sendMessage(Component.empty()
                .append(UtilsUtils.attribute("Precise", preciseJson, NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Block", blockJson, NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Rounded", roundedJson, NamedTextColor.WHITE))
        );
        return false;
    }
}
