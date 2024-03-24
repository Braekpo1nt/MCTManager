package org.braekpo1nt.mctmanager.commands.edit;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class EditSubCommand extends CommandManager {
    
    public EditSubCommand(GameManager gameManager) {
        subCommands.put("start", new StartSubCommand(gameManager));
        subCommands.put("stop", new StopSubCommand(gameManager));
        subCommands.put("validate", new ValidateSubCommand(gameManager));
        subCommands.put("save", new SaveSubCommand(gameManager));
        subCommands.put("load", new LoadSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct edit <options>");
    }
}
