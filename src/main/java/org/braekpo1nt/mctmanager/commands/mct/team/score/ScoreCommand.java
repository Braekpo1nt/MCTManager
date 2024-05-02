package org.braekpo1nt.mctmanager.commands.mct.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.OldCommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class ScoreCommand extends OldCommandManager {
    
    public ScoreCommand(GameManager gameManager) {
        subCommands.put("add", new ScoreAddSubCommand(gameManager));
        subCommands.put("subtract", new ScoreSubtractSubCommand(gameManager));
        subCommands.put("set", new ScoreSetSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("/mct team score <options>");
    }
}
