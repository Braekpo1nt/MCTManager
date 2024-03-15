package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.DisplayUtils;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.braekpo1nt.mctmanager.display.geometry.GeometryUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Must be a player to run this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        int duration = Integer.parseInt(args[0]); // in ticks
        
        float x1 = 1;
        float y1 = 1;
        float z1 = 1;
        float x2 = 10;
        float y2 = 10;
        float z2 = 10;
        if (args.length == 7) {
            x1 = Float.parseFloat(args[1]);
            y1 = Float.parseFloat(args[2]);
            z1 = Float.parseFloat(args[3]);
            x2 = Float.parseFloat(args[4]);
            y2 = Float.parseFloat(args[5]);
            z2 = Float.parseFloat(args[6]);
        }
        
        BoundingBox box = new BoundingBox(
                x1, y1, z1, 
                x2, y2, z2
        );
        List<Vector> points = GeometryUtils.toPoints(box, 10);
        DisplayUtils.display(plugin, player, points, duration);
        
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
