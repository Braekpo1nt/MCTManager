package org.braekpo1nt.mctmanager.commands.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

class VectorSubCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            sender.sendMessage(Component.text("Usage: /utils vector [<player>]")
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
        Vector precise = player.getLocation().toVector();
        String preciseJson = UtilsUtils.GSON.toJson(precise);
        Vector block = player.getLocation().toBlockLocation().toVector();
        String blockJson = UtilsUtils.GSON.toJson(block);
        sender.sendMessage(Component.empty()
                .append(attribute("Precise", preciseJson, NamedTextColor.WHITE))
                .append(attribute("Block", blockJson, NamedTextColor.WHITE))
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
}
