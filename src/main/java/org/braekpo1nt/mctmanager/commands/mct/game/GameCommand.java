package org.braekpo1nt.mctmanager.commands.mct.game;

import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class GameCommand extends CommandManager {
    
    public GameCommand(GameManager gameManager) {
        addSubCommand(new StartSubCommand(gameManager, "start"));
        addSubCommand(new StopSubCommand(gameManager, "stop"));
        addSubCommand(new VoteSubCommand(gameManager, "vote"));
        addSubCommand(new LoadSubCommand(gameManager, "load"));
    }
    
    @Override
    public @NotNull String getName() {
        return "game";
    }
}
