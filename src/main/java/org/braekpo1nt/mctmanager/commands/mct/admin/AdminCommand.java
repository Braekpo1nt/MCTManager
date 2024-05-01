package org.braekpo1nt.mctmanager.commands.mct.admin;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class AdminCommand extends CommandManager {
    
    public AdminCommand(GameManager gameManager) {
        subCommands.put("add", new AddSubCommand(gameManager));
        subCommands.put("remove", new RemoveSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct admin <options>");
    }
}
