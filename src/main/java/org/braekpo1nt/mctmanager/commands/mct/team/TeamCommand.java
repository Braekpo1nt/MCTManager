package org.braekpo1nt.mctmanager.commands.mct.team;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.commandmanager.OldCommandManager;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.mct.team.score.ScoreCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class TeamCommand extends CommandManager {
    
    public TeamCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new AddSubCommand(gameManager, "add"));
        addSubCommand(new JoinSubCommand(gameManager, "join"));
        addSubCommand(new LeaveSubCommand(gameManager, "leave"));
        addSubCommand(new ListSubCommand(gameManager, "list"));
        addSubCommand(new RemoveSubCommand(gameManager, "remove"));
//        addSubCommand(new ScoreCommand(gameManager, "score"));
    }
    
}
