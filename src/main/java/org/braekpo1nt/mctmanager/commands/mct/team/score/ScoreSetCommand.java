package org.braekpo1nt.mctmanager.commands.mct.team.score;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineParticipantResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineParticipantArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.TeamArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ScoreSetCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public ScoreSetCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("set")
                .then(Permissioned.literal("participant")
                        .then(Permissioned.argument("participantName", new OfflineParticipantArgumentType(gameManager))
                                .then(Permissioned.argument("score", IntegerArgumentType.integer(0))
                                        .executes(BrigadierAdapters.wrapsFuture(this::executeSetParticipant))
                                )
                        )
                )
                .then(Permissioned.literal("team")
                        .then(Permissioned.argument("teamId", new TeamArgumentType(gameManager))
                                .then(Permissioned.argument("score", IntegerArgumentType.integer(0))
                                        .executes(BrigadierAdapters.wrapsFuture(this::executeSetTeam))
                                )
                        )
                )
                .then(Permissioned.literal("all")
                        .then(Permissioned.argument("score", IntegerArgumentType.integer(0))
                                .executes(BrigadierAdapters.wrapsFuture(this::executeSetAll))
                        )
                )
                ;
    }
    
    private @NotNull CompletableFuture<CommandResult> executeSetParticipant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OfflineParticipant offlineParticipant = ctx.getArgument("participantName", OfflineParticipantResolver.class)
                .resolve();
        int score = ctx.getArgument("score", Integer.class);
        if (score < 0) {
            return CommandResult.failure(Component.text("Score must be at least 0")).asFuture();
        }
        return gameManager.setScore(offlineParticipant, score, "set score of participant command")
                .thenApply(newScore -> CommandResult.success(Component.empty()
                        .append(offlineParticipant.displayName())
                        .append(Component.text(" score is now "))
                        .append(Component.text(newScore))))
                ;
    }
    
    private @NotNull CompletableFuture<CommandResult> executeSetTeam(CommandContext<CommandSourceStack> ctx) {
        Team team = ctx.getArgument("teamId", Team.class);
        int score = ctx.getArgument("score", Integer.class);
        if (score < 0) {
            return CommandResult.failure(Component.text("Score must be at least 0")).asFuture();
        }
        return gameManager.setScore(team, score, "set score of team command")
                .thenApply(newScore -> CommandResult.success(Component.empty()
                        .append(team.getFormattedDisplayName())
                        .append(Component.text(" score is now "))
                        .append(Component.text(newScore))))
                ;
    }
    
    private @NotNull CompletableFuture<CommandResult> executeSetAll(CommandContext<CommandSourceStack> ctx) {
        int score = ctx.getArgument("score", Integer.class);
        if (score < 0) {
            return CommandResult.failure(Component.text("Score must be at least 0")).asFuture();
        }
        return gameManager.setScoreAll(score, "set score all command")
                .thenApply(v -> CommandResult.success(Component.empty()
                        .append(Component.text("All team and participant scores have been set to "))
                        .append(Component.text(score))));
        
    }
}
