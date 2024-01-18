package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.YawPitch;
import org.braekpo1nt.mctmanager.utils.EntityUtils;
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
        String arg = args[0];
        switch (arg) {
            case "setyaw" -> {
                if (args.length != 2) {
                    sender.sendMessage(Component.text("Usage: /mctdebug setyaw <yaw>")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                if (!CommandUtils.isFloat(args[1])) {
                    sender.sendMessage(Component.text(args[1])
                            .append(Component.text(" is not a number"))
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                float newYaw = Float.parseFloat(args[1]);
                Location location = player.getLocation();
                float oldYaw = location.getYaw();
                location.setYaw(newYaw);
                sender.sendMessage(Component.text("New yaw: ")
                        .append(Component.text(newYaw)));
                player.teleport(location);
                sender.sendMessage(Component.text("new yaw minus old yaw: ")
                        .append(Component.text(newYaw - oldYaw)));
    
            }
            case "lookat" -> {
                if (args.length != 4) {
                    sender.sendMessage(Component.text("Usage: /mctdebug lookat <x> <y> <z>")
                            .color(NamedTextColor.RED));
                    return true;
                }
    
                for (int i = 1; i < 4; i++) {
                    String coordinate = args[i];
                    if (!CommandUtils.isDouble(coordinate)) {
                        sender.sendMessage(Component.text(coordinate)
                                .append(Component.text(" is not a number"))
                                .color(NamedTextColor.RED));
                        return true;
                    }
                }
                
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                
                Vector source = player.getLocation().toVector();
                // adjust for player head height
                source = source.add(new Vector(0, .62, 0));
                Vector target = new Vector(x, y, z);
                // x-axis distance from source to target
                double deltaX = target.getX() - source.getX();
                // vertical distance from source to target
                double deltaY = target.getY() - source.getY();
                // z-axis distance from source to target
                double deltaZ = target.getZ() - source.getZ();
                // yaw (horizontal rotation angle) in degrees
                float yaw = (float) -Math.toDegrees(Math.atan2(deltaX, deltaZ));
                // horizontal distance from source to target
                double d = Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
                // pitch (vertical rotation angle) in degrees
                float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, d));
                
                sender.sendMessage(Component.text("yaw: ")
                        .append(Component.text(yaw)));
                sender.sendMessage(Component.text("pitch: ")
                        .append(Component.text(pitch)));
    
                Location location = player.getLocation();
                location.setYaw(yaw);
                location.setPitch(pitch);
                player.teleport(location);
            }
        }
    
        
        
//        player.teleport(source.toLocation(player.getWorld(), yawPitch.yaw(), yawPitch.pitch()));
        
//        Component mainTitle = Component.text("Main title");
//        Component subTitle = Component.text("Subtitle");
//
//        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
//        Title title = Title.title(mainTitle, subTitle, times);
//        sender.showTitle(title);
        return true;
    }
    
    // Method to teleport a player to their location but looking at a block
    public static void teleportPlayerLookingAtBlock(Player player, Location blockLocation) {
        // Get the player's location
        Location playerLocation = player.getLocation();
        
        // Calculate the direction vector from the player's location to the block's location
        Vector direction = blockLocation.toVector().subtract(playerLocation.toVector()).normalize();
        
        // Calculate the pitch and yaw angles from the direction vector using getBlockFace()
        float pitch = getPitch(direction);
        float yaw = getYaw(direction);
        
        // Set the pitch and yaw angles in the player's location
        playerLocation.setPitch(pitch);
        playerLocation.setYaw(yaw);
        
        // Teleport the player to their location with the updated pitch and yaw angles
        player.teleport(playerLocation);
    }
    
    // Method to calculate pitch from a direction vector
    private static float getPitch(Vector direction) {
        double x = direction.getX();
        double y = direction.getY();
        double z = direction.getZ();
        
        double horizontalDistance = Math.sqrt(x * x + z * z);
        return (float) Math.toDegrees(Math.atan2(-y, horizontalDistance));
    }
    
    // Method to calculate yaw from a direction vector
    private static float getYaw(Vector direction) {
        double x = direction.getX();
        double z = direction.getZ();
        
        return (float) Math.toDegrees(Math.atan2(z, -x));
    }
    
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
