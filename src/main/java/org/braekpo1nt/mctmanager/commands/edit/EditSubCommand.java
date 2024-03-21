package org.braekpo1nt.mctmanager.commands.edit;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class EditSubCommand extends CommandManager {
    
    public EditSubCommand(GameManager gameManager) {
        subCommands.put("start", new StartSubCommand(gameManager));
        subCommands.put("stop", new StopSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct edit <options>");
    }
}
