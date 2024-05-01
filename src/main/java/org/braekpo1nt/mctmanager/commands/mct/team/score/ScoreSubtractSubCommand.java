package org.braekpo1nt.mctmanager.commands.mct.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class ScoreSubtractSubCommand extends CommandManager {

    public ScoreSubtractSubCommand(GameManager gameManager) {
        subCommands.put("player", new ScoreAddPlayerSubCommand(gameManager, true));
        subCommands.put("team", new ScoreAddTeamSubCommand(gameManager, true));
    }

    @Override
    public Component getUsageMessage() {
        return Component.text("/mct team score subtract <player|team>");
    }
    
    
}
