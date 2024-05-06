package org.braekpo1nt.mctmanager.commands.mct.team.score;

import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreSubtractSubCommand extends CommandManager {

    public ScoreSubtractSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScoreAddPlayerSubCommand(gameManager, true, "player"));
//        addSubCommand(new ScoreAddTeamSubCommand(gameManager, true, "team"));
    }
}
