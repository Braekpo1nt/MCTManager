package org.braekpo1nt.mctmanager.commands.mct.team.score.set;

import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreSetCommand extends CommandManager {
    public ScoreSetCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScoreSetPlayerSubCommand(gameManager, "player"));
//        addSubCommand(new ScoreSetTeamSubCommand(gameManager, "team"));
    }
}
