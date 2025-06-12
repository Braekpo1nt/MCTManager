package org.braekpo1nt.mctmanager.commands.mctdebug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.display.BoxDisplay;
import org.braekpo1nt.mctmanager.display.Display;
import org.braekpo1nt.mctmanager.display.EdgeDisplay;
import org.braekpo1nt.mctmanager.display.geometry.Edge;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A utility command for testing various things, so I don't have to create a new command. 
 */
public class MCTDebugCommand implements TabExecutor, Listener {
    
    private final Main plugin;
    private final GameManager gameManager;
    
    public MCTDebugCommand(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        Objects.requireNonNull(this.plugin.getCommand("mctdebug")).setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Must be a player to run this command")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        String secondsStr = args[0];
        long seconds = Long.parseLong(secondsStr);
        
        Vector min = player.getLocation().toVector();
        Vector max = player.getLocation().toVector().add(new Vector(1, 2, 3));
        Display boxDisplay = new BoxDisplay(
                player.getWorld(), 
                new BoundingBox(
                        min.getX(),
                        min.getY(),
                        min.getZ(),
                        max.getX(), 
                        max.getY(), 
                        max.getZ()
                ),
                Material.GLASS
        );
        boxDisplay.show();
        
        Display edgeDisplay = new EdgeDisplay(player.getWorld(), new Edge(min, max), Material.RED_WOOL);
        edgeDisplay.show();
        player.sendMessage(Component.empty()
                .append(Component.text("Creating display at min: "))
                .append(Component.text(min.toString()))
                .append(Component.text(", max: "))
                .append(Component.text(max.toString()))
        );
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            boxDisplay.hide();
            edgeDisplay.hide();
            player.sendMessage("Hiding display");
        }, 20L*seconds);
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
