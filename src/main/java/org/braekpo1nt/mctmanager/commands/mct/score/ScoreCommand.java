package org.braekpo1nt.mctmanager.commands.mct.score;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.mct.score.player.ScorePlayerSubCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreCommand extends CommandManager {
    
    public ScoreCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScorePlayerSubCommand(gameManager, "player"));
    }
}
