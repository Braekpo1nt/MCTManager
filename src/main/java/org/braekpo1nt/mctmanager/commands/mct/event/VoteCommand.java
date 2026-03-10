package org.braekpo1nt.mctmanager.commands.mct.event;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
import org.jetbrains.annotations.NotNull;

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
                .then(Permissioned.argument("game", new EnumArgumentType<>(GameType.class, VoteManager.votableGames()))
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            GameType gameToAdd = ctx.getArgument("game", GameType.class);
                            return gameManager.addGameToVotingPool(gameToAdd);
                        }))
                )
                ;
    }
    
    private Permissioned<CommandSourceStack> buildRemove() {
        return Permissioned.literal("remove")
                .then(Permissioned.argument("game", new EnumArgumentType<>(GameType.class, VoteManager.votableGames()))
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            GameType gameToRemove = ctx.getArgument("game", GameType.class);
                            return gameManager.removeGameFromVotingPool(gameToRemove);
                        }))
                )
                ;
    }
}
