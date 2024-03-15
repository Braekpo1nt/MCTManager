package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
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
        
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        long ticks = Long.parseLong(args[0]); // 1 
        int duration = Integer.parseInt(args[1]); // in ticks
        int count = Integer.parseInt(args[2]); // 1
        float size = Float.parseFloat(args[3]); // 1.0
        
        float x1 = 1;
        float y1 = 1;
        float z1 = 1;
        float x2 = 10;
        float y2 = 10;
        float z2 = 10;
        if (args.length == 10) {
            x1 = Float.parseFloat(args[4]);
            y1 = Float.parseFloat(args[5]);
            z1 = Float.parseFloat(args[6]);
            x2 = Float.parseFloat(args[7]);
            y2 = Float.parseFloat(args[8]);
            z2 = Float.parseFloat(args[9]);
        }
        
        BoundingBox box = new BoundingBox(
                x1, y1, z1, 
                x2, y2, z2
        );
        List<Vector> points = GeometryUtils.toPoints(box, 10);
        new BukkitRunnable() {
            int timeLeft = duration;
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    this.cancel();
                    return;
                }
                displayPoints(points, player, count, size);
                timeLeft--;
            }
        }.runTaskTimerAsynchronously(plugin, 0L, ticks);
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    public static List<Vector> createHollowCube(BoundingBox area) {
        List<Vector> result = new ArrayList<>();
        int minX = area.getMin().getBlockX();
        int minY = area.getMin().getBlockY();
        int minZ = area.getMin().getBlockZ();
        int maxX = area.getMax().getBlockX();
        int maxY = area.getMax().getBlockY();
        int maxZ = area.getMax().getBlockZ();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (
                        x == minX || x == maxX
                        || y == minY || y == maxY
                        || z == minZ || z == maxZ
                    ) {
                        result.add(new Vector(x, y, z));
                    }
                }
            }
        }
        return result;
    }
    
    private void displayPoints(List<Vector> points, Player viewer, int count, float size) {
        for (Vector v : points) {
            viewer.spawnParticle(Particle.REDSTONE, v.getX(), v.getY(), v.getZ(), count, new Particle.DustOptions(Color.RED, size));
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
