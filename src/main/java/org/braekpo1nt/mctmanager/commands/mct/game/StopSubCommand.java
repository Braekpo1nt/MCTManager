package org.braekpo1nt.mctmanager.commands.mct.game;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.argumenttypes.ConfigFileArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.GameIdArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

public class StopSubCommand implements BrigadierSubCommand {
    
    private final static String GAME_ID_ARG = "gameId";
    
    private final @NotNull GameManager gameManager;
    
    public StopSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("stop")
                .executes(BrigadierAdapters.wraps(this::executeStopAll))
                .then(Permissioned.argument(GAME_ID_ARG, new GameIdArgumentType(gameManager, true))
                        .executes(BrigadierAdapters.wraps(this::executeStopGame))
                        .then(Permissioned.argument("configFile", new ConfigFileArgumentType(gameManager, true, GAME_ID_ARG))
                                .executes(BrigadierAdapters.wraps(this::executeStopGameConfig))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeStopAll(@NotNull CommandContext<CommandSourceStack> ctx) {
        return gameManager.stopAllGames();
    }
    
    private @NotNull CommandResult executeStopGame(@NotNull CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument(GAME_ID_ARG, GameType.class);
        return gameManager.stopGame(gameType, null);
    }
    
    private @NotNull CommandResult executeStopGameConfig(@NotNull CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument(GAME_ID_ARG, GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        return gameManager.stopGame(gameType, configFile);
    }
}
