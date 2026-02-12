package org.braekpo1nt.mctmanager.commands.mctdebug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

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
        
        String mode = args[0];
        
        try {
            switch (mode) {
                case "practice" -> {
                    gameManager.getGameStateService().rebuildPracticeMode();
                    sender.sendMessage("Loaded practice mode");
                }
                case "maintenance" -> {
                    gameManager.getGameStateService().rebuildMaintenanceMode();
                    sender.sendMessage("Loaded maintenance mode");
                }
                case "event" -> {
                    throw new UnsupportedOperationException("not yet implemented event");
                }
                default -> {
                    sender.sendMessage("not recognized practice|event|maintenance");
                    return true;
                }
            }
        } catch (SQLException e) {
            Main.logger().log(Level.SEVERE, "An error occurred trying to rebuild practice mode", e);
            sender.sendMessage("An error occurred, see the console for more");
            return true;
        }
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
