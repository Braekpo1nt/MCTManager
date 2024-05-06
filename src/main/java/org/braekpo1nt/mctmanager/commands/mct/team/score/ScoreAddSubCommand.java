package org.braekpo1nt.mctmanager.commands.mct.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.commandmanager.OldCommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreAddSubCommand extends CommandManager {

    public ScoreAddSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScoreAddPlayerSubCommand(gameManager, false, "player"));
//        addSubCommand(new ScoreAddTeamSubCommand(gameManager, false, "team"));
    }
}
