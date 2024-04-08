package org.braekpo1nt.mctmanager.commands.admin;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class AdminSubCommand extends CommandManager {
    
    public AdminSubCommand(GameManager gameManager) {
        subCommands.put("add", new AddSubCommand(gameManager));
        subCommands.put("remove", new RemoveSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct admin <options>");
    }
}
