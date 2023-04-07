package org.braekpo1nt.mctmanager.commands.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.CommandExecutor;

public class ScoreSubtractSubCommand extends CommandManager {

    public ScoreSubtractSubCommand(GameManager gameManager) {
        subCommands.put("player", new ScoreAddPlayerSubCommand(gameManager));
        subCommands.put("team", new ScoreAddTeamSubCommand(gameManager));
    }

    @Override
    protected Component getUsageMessage() {
        return Component.text("/mct team score subtract <player|team>");
    }
    
    
}
