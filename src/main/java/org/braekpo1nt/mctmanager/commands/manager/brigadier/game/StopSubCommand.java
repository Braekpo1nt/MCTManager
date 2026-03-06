package org.braekpo1nt.mctmanager.commands.manager.brigadier.game;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.argumenttypes.ConfigFileArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.GameIdArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class StopSubCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public StopSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("stop")
                .executes(BrigadierAdapters.wraps(this::executeStopAll))
                .then(Commands.argument("gameId", new GameIdArgumentType(gameManager, false))
                        .executes(BrigadierAdapters.wraps(this::executeStopGame))
                        .then(Commands.argument("configFile", new ConfigFileArgumentType(gameManager, false, "gameId"))
                                .executes(BrigadierAdapters.wraps(this::executeStopGameConfig))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeStopAll(@NotNull CommandContext<CommandSourceStack> ctx) {
        return gameManager.stopAllGames();
    }
    
    private @NotNull CommandResult executeStopGame(@NotNull CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument("gameId", GameType.class);
        return gameManager.stopGame(gameType, null);
    }
    
    private @NotNull CommandResult executeStopGameConfig(@NotNull CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument("gameId", GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        return gameManager.stopGame(gameType, configFile);
    }
}
