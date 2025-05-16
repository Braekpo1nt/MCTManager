package org.braekpo1nt.mctmanager.commands.mct.team.score;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.mct.team.score.add.ScoreAddCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.score.set.ScoreSetCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.score.subtract.ScoreSubtractCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreCommand extends CommandManager {
    
    public ScoreCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScoreAddCommand(gameManager, "add"));
        addSubCommand(new ScoreSubtractCommand(gameManager, "subtract"));
        addSubCommand(new ScoreSetCommand(gameManager, "set"));
    }
}
