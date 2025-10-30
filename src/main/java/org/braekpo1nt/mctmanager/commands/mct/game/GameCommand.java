package org.braekpo1nt.mctmanager.commands.mct.game;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;

public class GameCommand extends CommandManager {
    
    public GameCommand(Main plugin, GameManager gameManager) {
        super("game");
        addSubCommand(new StartSubCommand(plugin, gameManager, "start"));
        addSubCommand(new StopSubCommand(gameManager, "stop"));
        addSubCommand(new JoinSubCommand(gameManager, "join"));
        addSubCommand(new StatusSubCommand(plugin, gameManager, "status"));
    }
}
