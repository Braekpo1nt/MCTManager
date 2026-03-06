package org.braekpo1nt.mctmanager.commands.manager.brigadier.game;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class GameCommand implements BrigadierSubCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public GameCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("game")
                .then(new StartSubCommand(gameManager).create())
                .then(new StopSubCommand(gameManager).create())
                ;
    }
}
