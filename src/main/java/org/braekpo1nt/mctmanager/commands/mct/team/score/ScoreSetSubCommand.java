package org.braekpo1nt.mctmanager.commands.mct.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class ScoreSetSubCommand extends CommandManager {
    public ScoreSetSubCommand(GameManager gameManager) {
        subCommands.put("player", new ScoreSetPlayerSubCommand(gameManager));
        subCommands.put("team", new ScoreSetTeamSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("/mct team score set <player|team>");
    }
    
}
