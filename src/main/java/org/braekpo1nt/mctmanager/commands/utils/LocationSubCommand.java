package org.braekpo1nt.mctmanager.commands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.SubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.braekpo1nt.mctmanager.utils.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

class LocationSubCommand extends TabSubCommand {
    
    public LocationSubCommand(@NotNull String name) {
        super(name);
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            return CommandResult.failure(getUsage().of("[<playerName>]"));
        }
        
        if (args.length == 1) {
            String playerName = args[0];
            Player player = Bukkit.getPlayer(playerName);
            if (player == null) {
                return CommandResult.failure(Component.text("Player ")
                        .append(Component.text(playerName)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" not found")));
            }
            displayLocation(sender, player.getLocation());
            return CommandResult.success();
        }
        
        if (!(sender instanceof Player player)) {
            return CommandResult.failure(Component.text("Only a player can use this command"));
        }
        displayLocation(sender, player.getLocation());
        return CommandResult.success();
    }
    
    private void displayLocation(@NotNull CommandSender sender, Location location) {
        LocationDTO precise = new LocationDTO(location);
        String preciseJson = UtilsUtils.GSON.toJson(precise);
        
        Location blockLocation = location.toBlockLocation();
        blockLocation.setYaw((location.getYaw()));
        blockLocation.setPitch(location.getPitch());
        LocationDTO block = new LocationDTO(blockLocation);
        String blockJson = UtilsUtils.GSON.toJson(block);
        
        LocationDTO rounded = new LocationDTO(
                MathUtils.specialRound(location, 0.5, 45)
        );
        String roundedJson = UtilsUtils.GSON.toJson(rounded);
        
        sender.sendMessage(Component.empty()
                .append(UtilsUtils.attribute("Precise", preciseJson, NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Block", blockJson, NamedTextColor.WHITE))
                .append(UtilsUtils.attribute("Rounded", roundedJson, NamedTextColor.WHITE))
        );
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return null;
        }
        return Collections.emptyList();
    }
}
