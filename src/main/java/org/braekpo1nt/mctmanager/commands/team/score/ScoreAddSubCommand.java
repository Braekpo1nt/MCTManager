package org.braekpo1nt.mctmanager.commands.team.score;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;

import java.util.Map;

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
