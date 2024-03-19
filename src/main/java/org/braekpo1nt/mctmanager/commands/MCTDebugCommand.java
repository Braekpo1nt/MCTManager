package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.DisplayUtils;
import org.braekpo1nt.mctmanager.display.geometry.GeometryUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    private final Main plugin;
    private BoundingBox detectionBox = null;
    
    public MCTDebugCommand(Main plugin) {
        this.plugin = plugin;
        plugin.getCommand("mctdebug").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        double distance = Double.parseDouble(args[1]); // in blocks
        
        float x1 = 1;
        float y1 = 1;
        float z1 = 1;
        float x2 = 10;
        float y2 = 10;
        float z2 = 10;
        if (args.length == 8) {
            x1 = Float.parseFloat(args[2]);
            y1 = Float.parseFloat(args[3]);
            z1 = Float.parseFloat(args[4]);
            x2 = Float.parseFloat(args[5]);
            y2 = Float.parseFloat(args[6]);
            z2 = Float.parseFloat(args[7]);
        }
        
        detectionBox = new BoundingBox(
                x1, y1, z1, 
                x2, y2, z2
        );
        List<Vector> points = GeometryUtils.toRectanglePoints(detectionBox, distance);
        DisplayUtils.display(plugin, player, points, duration);
        roller = 0;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            detectionBox = null;
        }, duration);
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    private int roller = 0;
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (detectionBox == null) {
            return;
        }
        if (detectionBox.contains(player.getLocation().toVector())) {
            player.sendMessage(Component.text("Detected ")
                    .append(Component.text(roller)));
        }
        roller++;
        if (roller > 9) {
            roller = 0;
        }
    }
    
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
