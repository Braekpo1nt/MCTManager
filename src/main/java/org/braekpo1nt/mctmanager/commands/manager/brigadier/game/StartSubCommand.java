package org.braekpo1nt.mctmanager.commands.manager.brigadier.game;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
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
    
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public StartSubCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("start")
                .then(Commands.argument("gameId", new EnumArgumentType<>(GameType.class, GameType.values()))
                        .executes(BrigadierAdapters.wraps(this::executeStartDefault))
                        .then(Commands.argument("configFile", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandUtils.suggestConfigFiles(plugin, ctx, builder))
                                .executes(BrigadierAdapters.wraps(this::executeStartConfig))
                                .then(Commands.argument("teamIds", TeamIdsListArgumentType.teamIds(gameManager))
                                        .executes(BrigadierAdapters.wraps(this::executeStartTeamIds))
                                )
                        )
                )
                ;
    }
    
    private CommandResult executeStartDefault(CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument("gameId", GameType.class);
        return gameManager.startGame(gameType, "default.json");
    }
    
    private CommandResult executeStartConfig(CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument("gameId", GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        return gameManager.startGame(gameType, configFile);
    }
    
    private @NotNull CommandResult executeStartTeamIds(CommandContext<CommandSourceStack> ctx) {
        GameType gameType = ctx.getArgument("gameId", GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
        String[] teamIds = ctx.getArgument("teamIds", String[].class);
        return gameManager.startGame(Arrays.stream(teamIds).collect(Collectors.toSet()), Collections.emptyList(), gameType, configFile);
    }
    
}
