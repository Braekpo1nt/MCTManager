package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Retrieves the saved preset file and performs the equivalent 
 * of executing the commands in order to achieve the specified GameState. 
 */
public class PresetApplySubCommand extends SubCommand {
    
    private final PresetController controller;
    private final GameManager gameManager;
    
    public PresetApplySubCommand(GameManager gameManager, PresetController controller, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
        this.controller = controller;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        
        return CommandResult.success();
    }
}
