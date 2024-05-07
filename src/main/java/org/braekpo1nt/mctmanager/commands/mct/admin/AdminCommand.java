package org.braekpo1nt.mctmanager.commands.mct.admin;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class AdminCommand extends CommandManager {
    
    public AdminCommand(GameManager gameManager) {
        super("admin");
        addSubCommand(new AddSubCommand(gameManager, "add"));
        addSubCommand(new RemoveSubCommand(gameManager, "remove"));
    }
}
