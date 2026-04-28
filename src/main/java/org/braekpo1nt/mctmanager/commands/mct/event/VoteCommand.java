package org.braekpo1nt.mctmanager.commands.mct.event;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.argumenttypes.GameIdArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VoteCommand implements BrigadierSubCommand {
    
    public final @NotNull GameManager gameManager;
    
    public VoteCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("vote")
                .then(buildAdd())
                .then(buildRemove())
                ;
    }
    
    private Permissioned<CommandSourceStack> buildAdd() {
        return Permissioned.literal("add")
                .then(Permissioned.argument("game", new GameIdArgumentType(gameManager, false, VoteManager.votableGames()))
                        .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                            List<GameType> votingPool = gameManager.getVotingPool();
                            VoteManager.votableGames().stream()
                                    .filter(gameType -> !votingPool.contains(gameType))
                                    .forEach(gameType -> builder.suggest(gameType.getId()));
                            return builder.build();
                        }))
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            GameType gameToAdd = ctx.getArgument("game", GameType.class);
                            return gameManager.addGameToVotingPool(gameToAdd);
                        }))
                )
                ;
    }
    
    private Permissioned<CommandSourceStack> buildRemove() {
        return Permissioned.literal("remove")
                .then(Permissioned.argument("game", new GameIdArgumentType(gameManager, false, VoteManager.votableGames()))
                        .suggests((ctx, builder) -> CompletableFuture.supplyAsync(() -> {
                            gameManager.getVotingPool()
                                    .forEach(gameType -> builder.suggest(gameType.getId()));
                            return builder.build();
                        }))
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            GameType gameToRemove = ctx.getArgument("game", GameType.class);
                            return gameManager.removeGameFromVotingPool(gameToRemove);
                        }))
                )
                ;
    }
}
