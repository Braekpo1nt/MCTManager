package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.admin.AdminSubCommand;
import org.braekpo1nt.mctmanager.commands.event.EventSubCommand;
import org.braekpo1nt.mctmanager.commands.game.GameSubCommand;
import org.braekpo1nt.mctmanager.commands.team.TeamSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.bukkit.Bukkit;

import java.io.IOException;

/**
 * The super command for all MCT related commands. 
 * Everything should start with /mct _____, where _____ is a sub command
 */
public class MCTCommand extends CommandManager {
    
    public MCTCommand(Main plugin, GameManager gameManager, BlockEffectsListener blockEffectsListener) {
        plugin.getCommand("mct").setExecutor(this);
        subCommands.put("game", new GameSubCommand(gameManager));
        subCommands.put("option", new OptionSubCommand(gameManager, blockEffectsListener));
        subCommands.put("team", new TeamSubCommand(gameManager));
        subCommands.put("admin", new AdminSubCommand(gameManager));
        subCommands.put("event", new EventSubCommand(gameManager));
        subCommands.put("save", (sender, command, label, args) -> {
            gameManager.saveGameState();
            sender.sendMessage("Saved game state.");
            return true;
        });
        subCommands.put("load", (sender, command, label, args) -> {
            gameManager.loadGameState();
            sender.sendMessage("Loaded game state.");
            return true;
        });
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct <option>");
    }
}
