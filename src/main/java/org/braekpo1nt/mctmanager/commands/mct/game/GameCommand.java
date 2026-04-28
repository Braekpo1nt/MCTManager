package org.braekpo1nt.mctmanager.commands.mct.game;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;


public class GameCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public GameCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("game")
                .then(new StartSubCommand(gameManager).create())
                .then(new StopSubCommand(gameManager).create())
                .then(new JoinSubCommand(gameManager).create())
                .then(new StatusSubCommand(gameManager).create())
                ;
    }
}
