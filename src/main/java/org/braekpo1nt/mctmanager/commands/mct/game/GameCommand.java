package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.SubCommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;

public class GameCommand extends SubCommandManager {
    
    public GameCommand(GameManager gameManager) {
        subCommands.put("start", new StartSubCommand(gameManager));
//        subCommands.put("stop", new StopSubCommand(gameManager));
//        subCommands.put("vote", new VoteSubCommand(gameManager));
//        subCommands.put("load", new LoadSubCommand(gameManager));
    }
    
    @Override
    public Component getUsageMessage() {
        return Component.text("Usage: /mct game <options>");
    }
}
