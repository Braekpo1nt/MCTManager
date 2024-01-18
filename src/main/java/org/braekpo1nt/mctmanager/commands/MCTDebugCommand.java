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
