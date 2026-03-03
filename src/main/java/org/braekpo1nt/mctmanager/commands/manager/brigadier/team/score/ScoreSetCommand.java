package org.braekpo1nt.mctmanager.commands.manager.brigadier.team.score;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
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

public class ScoreSetCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public ScoreSetCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("set")
                .then(Commands.literal("participant")
                        .then(Commands.argument("participantName", new OfflineParticipantArgumentType(gameManager))
                                .then(Commands.argument("score", IntegerArgumentType.integer(0))
                                        .executes(BrigadierAdapters.wraps(this::executeSetParticipant))
                                )
                        )
                )
                .then(Commands.literal("team")
                        .then(Commands.argument("teamId", new TeamArgumentType(gameManager))
                                .then(Commands.argument("score", IntegerArgumentType.integer(0))
                                        .executes(BrigadierAdapters.wraps(this::executeSetTeam))
                                )
                        )
                )
                .then(Commands.literal("all")
                        .then(Commands.argument("score", IntegerArgumentType.integer(0))
                                .executes(BrigadierAdapters.wraps(this::executeSetAll))
                        )
                )
                ;
    }
    
    private CommandResult executeSetParticipant(CommandContext<CommandSourceStack> ctx) {
        OfflineParticipant offlineParticipant = ctx.getArgument("participantName", OfflineParticipant.class);
        int score = ctx.getArgument("score", Integer.class);
        if (score < 0) {
            return CommandResult.failure(Component.text("Score must be at least 0"));
        }
        int newScore = gameManager.setScore(offlineParticipant, score, "set score of participant command");
        return CommandResult.success(Component.empty()
                .append(offlineParticipant.displayName())
                .append(Component.text(" score is now "))
                .append(Component.text(newScore)));
    }
    
    private CommandResult executeSetTeam(CommandContext<CommandSourceStack> ctx) {
        Team team = ctx.getArgument("teamId", Team.class);
        int score = ctx.getArgument("score", Integer.class);
        if (score < 0) {
            return CommandResult.failure(Component.text("Score must be at least 0"));
        }
        int newScore = gameManager.setScore(team, score, "set score of team command");
        return CommandResult.success(Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(" score is now "))
                .append(Component.text(newScore)));
    }
    
    private CommandResult executeSetAll(CommandContext<CommandSourceStack> ctx) {
        int score = ctx.getArgument("score", Integer.class);
        if (score < 0) {
            return CommandResult.failure(Component.text("Score must be at least 0"));
        }
        gameManager.setScoreAll(score, "set score all command");
        
        return CommandResult.success(Component.empty()
                .append(Component.text("All team and participant scores have been set to "))
                .append(Component.text(score)));
    }
}
