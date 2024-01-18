package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor {
    
    private final Main plugin;
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("mctdebug").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        if (!(sender instanceof Player player)) {
//            sender.sendMessage(Component.text("Must be a player to run this command")
//                    .color(NamedTextColor.RED));
//            return true;
//        }
        
//        if (args.length < 1) {
//            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
//                    .color(NamedTextColor.RED));
//            return true;
//        }
        
//        player.teleport(source.toLocation(player.getWorld(), yawPitch.yaw(), yawPitch.pitch()));
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
