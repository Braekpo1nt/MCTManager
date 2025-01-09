package org.braekpo1nt.mctmanager.commands.mct.team.score.award;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreAwardCommand extends CommandManager {
    public ScoreAwardCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScoreAwardPlayersSubCommand(gameManager));
        addSubCommand(new ScoreAwardTeamsSubCommand(gameManager));
    }
    
}
