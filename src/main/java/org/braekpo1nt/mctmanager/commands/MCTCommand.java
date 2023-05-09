package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.event.EventSubCommand;
import org.braekpo1nt.mctmanager.commands.game.GameSubCommand;
import org.braekpo1nt.mctmanager.commands.team.TeamSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.braekpo1nt.mctmanager.hub.HubBoundaryListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The super command for all MCT related commands. 
 * Everything should start with /mct _____, where _____ is a sub command
 */
public class MCTCommand extends CommandManager {

    public MCTCommand(Main plugin, GameManager gameManager, HubBoundaryListener hubBoundaryListener, BlockEffectsListener blockEffectsListener) {
        plugin.getCommand("mct").setExecutor(this);
        subCommands.put("game", new GameSubCommand(gameManager));
        subCommands.put("option", new OptionSubCommand(hubBoundaryListener, blockEffectsListener));
        subCommands.put("team", new TeamSubCommand(gameManager));
        subCommands.put("event", new EventSubCommand(gameManager));
        subCommands.put("save", (sender, command, label, args) -> {
            try {
                gameManager.saveGameState();
                sender.sendMessage("Saved game state.");
            } catch (IOException e) {
                Bukkit.getLogger().severe("[MCTManager] Unable to save game state.");
                throw new RuntimeException(e);
            }
            return true;
        });
        subCommands.put("load", (sender, command, label, args) -> {
            try {
                gameManager.loadGameState();
                sender.sendMessage("Loaded game state.");
            } catch (IOException e) {
                Bukkit.getLogger().severe("[MCTManager] Unable to load game state.");
                throw new RuntimeException(e);
            }
            return true;
        });
    }
    
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct <option>");
    }
}
