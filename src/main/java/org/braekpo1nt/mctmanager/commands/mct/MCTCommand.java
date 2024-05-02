package org.braekpo1nt.mctmanager.commands.mct;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.commandmanager.NewCommandManager;
import org.braekpo1nt.mctmanager.commands.mct.admin.AdminCommand;
import org.braekpo1nt.mctmanager.commands.mct.edit.EditCommand;
import org.braekpo1nt.mctmanager.commands.mct.event.EventCommand;
import org.braekpo1nt.mctmanager.commands.mct.game.GameCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.TeamCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;

/**
 * The super command for all MCT related commands. 
 * Everything should start with /mct _____, where _____ is a sub command
 */
public class MCTCommand extends NewCommandManager {
    
    public MCTCommand(Main plugin, GameManager gameManager, BlockEffectsListener blockEffectsListener) {
        plugin.getCommand("mct").setExecutor(this);
        subCommands.put("game", new GameCommand(gameManager));
//        subCommands.put("edit", new EditCommand(gameManager));
//        subCommands.put("option", new OptionSubCommand(gameManager, blockEffectsListener));
//        subCommands.put("team", new TeamCommand(gameManager));
//        subCommands.put("admin", new AdminCommand(gameManager));
//        subCommands.put("event", new EventCommand(gameManager));
//        subCommands.put("save", (sender, command, label, args) -> {
//            gameManager.saveGameState();
//            sender.sendMessage("Saved game state.");
//            return true;
//        });
//        subCommands.put("load", (sender, command, label, args) -> {
//            gameManager.loadGameState();
//            sender.sendMessage("Loaded gameState.json");
//            return true;
//        });
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct <option>");
    }
}
