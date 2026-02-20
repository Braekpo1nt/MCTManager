package org.braekpo1nt.mctmanager.commands.mctdebug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.database.service.ScoreService;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
        
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                    .color(NamedTextColor.RED));
            return true;
        }
        
        String type = args[0];
        switch (type) {
            case "rebuild" -> {
                if (args.length != 2) {
                    sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                            .color(NamedTextColor.RED));
                    return true;
                }
                Mode mode = Mode.fromName(args[1]);
                
                try {
                    switch (mode) {
                        case PRACTICE -> {
                            gameManager.getGameStateService().rebuildPracticeMode();
                            sender.sendMessage("Loaded practice mode");
                        }
                        case MAINTENANCE -> {
                            gameManager.getGameStateService().rebuildMaintenanceMode();
                            sender.sendMessage("Loaded maintenance mode");
                        }
                        case EVENT -> {
                            gameManager.getGameStateService().rebuildEventMode("MCT_1B");
                            sender.sendMessage("Loaded event mode");
                        }
                        case null, default -> {
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
            case "totals" -> {
                if (args.length != 2) {
                    sender.sendMessage(Component.text("Usage: /mctdebug <arg> [options]")
                            .color(NamedTextColor.RED));
                    return true;
                }
                
                if (!CommandUtils.isInteger(args[1])) {
                    sender.sendMessage("Not an integer");
                    return true;
                }
                
                try {
                    int sessionId = Integer.parseInt(args[1]);
                    Map<UUID, ScoreService.PointTotal> participantSessionTotals = gameManager.getScoreService().getParticipantSessionTotals(sessionId);
                    sender.sendMessage(Component.empty()
                            .append(Component.text("Score totals for "))
                            .append(Component.text(sessionId))
                            .append(Component.text(":")));
                    for (ScoreService.PointTotal pointTotal : participantSessionTotals.values()) {
                        sender.sendMessage(Component.empty()
                                .append(Component.text(pointTotal.participantUUID().toString()))
                                .append(Component.text(", "))
                                .append(Component.text(pointTotal.teamId()))
                                .append(Component.text(" -> "))
                                .append(Component.text(pointTotal.totalPoints())));
                    }
                    
                    Map<String, Integer> teamSessionTotals = gameManager.getScoreService().getTeamSessionTotals(sessionId);
                    for (Map.Entry<String, Integer> entry : teamSessionTotals.entrySet()) {
                        sender.sendMessage(Component.empty()
                                .append(Component.text(entry.getKey()))
                                .append(Component.text(" -> "))
                                .append(Component.text(entry.getValue()))
                        );
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                
            }
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
