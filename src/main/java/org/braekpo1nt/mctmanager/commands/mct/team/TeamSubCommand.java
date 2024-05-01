package org.braekpo1nt.mctmanager.commands.mct.team;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.mct.team.score.ScoreCommand;
import org.braekpo1nt.mctmanager.games.GameManager;

public class TeamSubCommand extends CommandManager {
    
    public TeamSubCommand(GameManager gameManager) {
        subCommands.put("add", new AddSubCommand(gameManager));
        subCommands.put("join", new JoinSubCommand(gameManager));
        subCommands.put("leave", new LeaveSubCommand(gameManager));
        subCommands.put("list", new ListSubCommand(gameManager));
        subCommands.put("remove", new RemoveSubCommand(gameManager));
        subCommands.put("score", new ScoreCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct team <options>");
    }
}
