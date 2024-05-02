package org.braekpo1nt.mctmanager.commands.mct;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.mct.admin.AdminCommand;
import org.braekpo1nt.mctmanager.commands.mct.edit.EditCommand;
import org.braekpo1nt.mctmanager.commands.mct.game.GameCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.TeamCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.listeners.BlockEffectsListener;
import org.bukkit.command.PluginCommand;

/**
 * The super command for all MCT related commands. 
 * Everything should start with /mct _____, where _____ is a sub command
 */
public class MCTCommand extends CommandManager {
    
    public MCTCommand(Main plugin, GameManager gameManager, BlockEffectsListener blockEffectsListener) {
        super("mct");
        PluginCommand command = plugin.getCommand(getName());
        Preconditions.checkArgument(command != null, "Can't find command %s", getName());
        command.setExecutor(this);
        addSubCommand(new GameCommand(gameManager));
        addSubCommand(new EditCommand(gameManager, "edit"));
//        subCommands.put("option", new OptionSubCommand(gameManager, blockEffectsListener));
        addSubCommand(new TeamCommand(gameManager, "team"));
        addSubCommand(new AdminCommand(gameManager));
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
}
