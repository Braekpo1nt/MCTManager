package org.braekpo1nt.mctmanager.commands.manager.brigadier.team.score;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class ScoreCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public ScoreCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("score")
                .then(new ScoreAddCommand(gameManager, false).create())
                .then(new ScoreAddCommand(gameManager, true).create())
                .then(new ScoreSetCommand(gameManager).create())
                ;
    }
}
