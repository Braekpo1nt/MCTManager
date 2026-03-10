package org.braekpo1nt.mctmanager.commands.mct.stats;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.jetbrains.annotations.NotNull;

public class StatsCommand implements BrigadierSubCommand {
    
    private final @NotNull GameStateService gameStateService;
    
    public StatsCommand(@NotNull GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("stats")
                .then(new PlayerStatsCommand(gameStateService).create())
                ;
    }
    
    
}
