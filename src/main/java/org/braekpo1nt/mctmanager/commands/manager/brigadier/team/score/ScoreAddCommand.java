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

public class ScoreAddCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    private final boolean invert;
    
    public ScoreAddCommand(@NotNull GameManager gameManager, boolean invert) {
        this.gameManager = gameManager;
        this.invert = invert;
    }
    
    @Override
    public @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal(invert ? "subtract" : "add")
                .then(Commands.literal("participant")
                        .then(Commands.argument("participantName", new OfflineParticipantArgumentType(gameManager))
                                .then(Commands.argument("score", IntegerArgumentType.integer())
                                        .executes(BrigadierAdapters.wraps(this::executeAddParticipant))
                                )
                        )
                )
                .then(Commands.literal("team")
                        .then(Commands.argument("teamId", new TeamArgumentType(gameManager))
                                .then(Commands.argument("score", IntegerArgumentType.integer())
                                        .executes(BrigadierAdapters.wraps(this::executeAddTeam))
                                )
                        )
                )
                ;
    }
    
    private CommandResult executeAddParticipant(CommandContext<CommandSourceStack> ctx) {
        OfflineParticipant offlineParticipant = ctx.getArgument("participantName", OfflineParticipant.class);
        int score = ctx.getArgument("score", Integer.class);
        int currentScore = offlineParticipant.getScore();
        if (invert) {
            score = -score;
        }
        if (currentScore + score < 0) {
            score = -currentScore;
        }
        int newScore = gameManager.addScore(offlineParticipant, score, "add score to participant command");
        return CommandResult.success(Component.empty()
                .append(offlineParticipant.displayName())
                .append(Component.text(" score is now "))
                .append(Component.text(newScore)));
    }
    
    private CommandResult executeAddTeam(CommandContext<CommandSourceStack> ctx) {
        Team team = ctx.getArgument("teamId", Team.class);
        int score = ctx.getArgument("score", Integer.class);
        int currentScore = team.getScore();
        if (invert) {
            score = -score;
        }
        if (currentScore + score < 0) {
            score = -currentScore;
        }
        int newScore = gameManager.addScore(team, score, "add score to team command");
        return CommandResult.success(Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(" score is now "))
                .append(Component.text(newScore)));
    }
}
