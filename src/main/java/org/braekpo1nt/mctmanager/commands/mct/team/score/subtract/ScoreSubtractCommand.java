package org.braekpo1nt.mctmanager.commands.mct.team.score.subtract;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.mct.team.score.add.ScoreAddPlayerSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.score.add.ScoreAddTeamSubCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreSubtractCommand extends CommandManager {
    
    public ScoreSubtractCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScoreAddPlayerSubCommand(gameManager, true, "player"));
        addSubCommand(new ScoreAddTeamSubCommand(gameManager, true, "team"));
    }
}
