package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.braekpo1nt.mctmanager.commands.argumenttypes.EnumArgumentType;
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
    public LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("vote")
                .then(buildAdd())
                ;
    }
    
    private ArgumentBuilder<CommandSourceStack, ?> buildAdd() {
        return Commands.literal("add")
                .then(Commands.argument("game", new EnumArgumentType<>(GameType.class, VoteManager.votableGames()))
                        .executes(BrigadierAdapters.wraps(ctx -> {
                            GameType gameToAdd = ctx.getArgument("game", GameType.class);
                            return gameManager.addGameToVotingPool(gameToAdd);
                        }))
                )
                ;
    }
}
