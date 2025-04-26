package org.braekpo1nt.mctmanager.commands.mct.game;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class GameCommand extends CommandManager {
    
    public GameCommand(GameManager gameManager) {
        super("game");
        addSubCommand(new StartSubCommand(gameManager, "start"));
        addSubCommand(new StopSubCommand(gameManager, "stop"));
        addSubCommand(new JoinSubCommand(gameManager, "join"));
    }
}
