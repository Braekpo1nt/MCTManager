package org.braekpo1nt.mctmanager.commands.game;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.CommandExecutor;

import java.util.HashMap;
import java.util.Map;

public class GameSubCommand extends CommandManager {
    
    public GameSubCommand(GameManager gameManager) {
        subCommands.put("start", new StartSubCommand(gameManager));
        subCommands.put("stop", new StopSubCommand(gameManager));
        subCommands.put("vote", new VoteSubCommand(gameManager));
        subCommands.put("load", new LoadSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct game <options>");
    }
}
