package org.braekpo1nt.mctmanager.commands.mct.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class ScoreAddSubCommand extends CommandManager {

    public ScoreAddSubCommand(GameManager gameManager) {
        subCommands.put("player", new ScoreAddPlayerSubCommand(gameManager, false));
        subCommands.put("team", new ScoreAddTeamSubCommand(gameManager, false));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("/mct team score add <player|team>");
    }
}
