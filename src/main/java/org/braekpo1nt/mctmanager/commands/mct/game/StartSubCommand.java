package org.braekpo1nt.mctmanager.commands.mct.game;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.argumenttypes.ConfigFileArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.GameIdArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.TeamIdsListArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class StartSubCommand implements BrigadierSubCommand {
    
    private final static String GAME_ID_ARG = "gameId";
    
    private final @NotNull GameManager gameManager;
    
    public StartSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("start")
                .then(Permissioned.argument(GAME_ID_ARG, new GameIdArgumentType(gameManager, false))
                        .executes(BrigadierAdapters.wraps(this::executeStartDefault))
                        .then(Permissioned.argument("configFile", new ConfigFileArgumentType(gameManager, false, GAME_ID_ARG))
                                .executes(BrigadierAdapters.wraps(this::executeStartConfig))
                                .then(Permissioned.argument("teamIds", TeamIdsListArgumentType.teamIds(gameManager))
                                        .executes(BrigadierAdapters.wraps(this::executeStartTeamIds))
                                )
                        )
                )
                ;
    }
    
    private CommandResult executeStartDefault(CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument(GAME_ID_ARG, GameType.class);
        return gameManager.startGame(gameType, "default.json");
    }
    
    private CommandResult executeStartConfig(CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument(GAME_ID_ARG, GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        return gameManager.startGame(gameType, configFile);
    }
    
    private @NotNull CommandResult executeStartTeamIds(CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument(GAME_ID_ARG, GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        String[] teamIds = ctx.getArgument("teamIds", String[].class);
        return gameManager.startGame(Arrays.stream(teamIds).collect(Collectors.toSet()), Collections.emptyList(), gameType, configFile);
    }
    
}
