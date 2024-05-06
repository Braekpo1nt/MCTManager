package org.braekpo1nt.mctmanager.commands.mct.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.commandmanager.OldCommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreCommand extends CommandManager {
    
    public ScoreCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new ScoreAddSubCommand(gameManager, "add"));
        addSubCommand(new ScoreSubtractSubCommand(gameManager, "subtract"));
//        addSubCommand(new ScoreSetSubCommand(gameManager, "set"));
    }
}
