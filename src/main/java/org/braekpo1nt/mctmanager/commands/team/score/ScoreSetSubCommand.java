package org.braekpo1nt.mctmanager.commands.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.CommandExecutor;

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
