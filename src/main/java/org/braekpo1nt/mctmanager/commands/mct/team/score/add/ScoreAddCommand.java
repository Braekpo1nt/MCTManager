package org.braekpo1nt.mctmanager.commands.mct.team.score.add;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreAddCommand extends CommandManager {

    public ScoreAddCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScoreAddPlayerSubCommand(gameManager, false, "player"));
        addSubCommand(new ScoreAddTeamSubCommand(gameManager, false, "team"));
    }
}
