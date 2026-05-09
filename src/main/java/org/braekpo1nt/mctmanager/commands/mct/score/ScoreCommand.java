package org.braekpo1nt.mctmanager.commands.mct.score;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineParticipantArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.OfflineParticipantResolver;
import org.braekpo1nt.mctmanager.commands.argumenttypes.TeamArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScoreCommand implements BrigadierSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public ScoreCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("score")
                .executes(BrigadierAdapters.wraps(this::executeScoreSelf))
                .then(Commands.literal("player")
                        .then(Commands.argument("participant", new OfflineParticipantArgumentType(gameManager))
                                .executes(BrigadierAdapters.wraps(this::executeScorePlayer))
                        )
                )
                .then(Commands.literal("team")
                        .then(Commands.argument("teamId", new TeamArgumentType(gameManager))
                                .executes(BrigadierAdapters.wraps(this::executeScoreTeam))
                        )
                )
                .then(Commands.literal("all")
                        .executes(BrigadierAdapters.wraps(this::executeScoreAll))
                        .then(Commands.literal("players")
                                .executes(BrigadierAdapters.wraps(this::executeScoreAllPlayers))
                        )
                        .then(Commands.literal("teams")
                                .executes(BrigadierAdapters.wraps(this::executeScoreAllTeams))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeScoreSelf(@NotNull CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return CommandResult.failure("Only players can use the no-args command");
        }
        OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(player.getUniqueId());
        if (offlineParticipant == null) {
            return CommandResult.failure(Component.text("You are not a participant"));
        }
        return CommandResult.success(Component.empty()
                .append(player.displayName())
                .append(Component.text(": "))
                .append(Component.text(offlineParticipant.getScore())
                        .color(NamedTextColor.GOLD)));
    }
    
    private @NotNull CommandResult executeScorePlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OfflineParticipant offlineParticipant = ctx.getArgument("participant", OfflineParticipantResolver.class)
                .resolve();
        Main.logf("test score command to ");
        return CommandResult.success(Component.empty()
                .append(offlineParticipant.displayName())
                .append(Component.text(": "))
                .append(Component.text(offlineParticipant.getScore())
                        .color(NamedTextColor.GOLD)));
    }
    
    private @NotNull CommandResult executeScoreTeam(@NotNull CommandContext<CommandSourceStack> ctx) {
        Team team = ctx.getArgument("teamId", Team.class);
        return CommandResult.success(Component.empty()
                .append(team.getFormattedDisplayName())
                .append(Component.text(": "))
                .append(Component.text(team.getScore())
                        .color(NamedTextColor.GOLD)));
    }
    
    private @NotNull CommandResult executeScoreAll(@NotNull CommandContext<CommandSourceStack> ctx) {
        return CommandResult.success(GameManagerUtils.getTeamDisplay(gameManager));
    }
    
    private @NotNull CommandResult executeScoreAllPlayers(@NotNull CommandContext<CommandSourceStack> ctx) {
        return CommandResult.success(getAllPlayersScores());
    }
    
    private @NotNull CommandResult executeScoreAllTeams(@NotNull CommandContext<CommandSourceStack> ctx) {
        return CommandResult.success(getAllTeamScores());
    }
    
    /**
     * @return a component with all players and their scores in order from highest to lowest, ties broken alphabetically.
     */
    private Component getAllPlayersScores() {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Player Scores (Un-multiplied):")
                        .decorate(TextDecoration.BOLD));
        List<OfflineParticipant> sortedOfflinePlayers = GameManagerUtils.getSortedOfflineParticipants(gameManager);
        for (OfflineParticipant participant : sortedOfflinePlayers) {
            builder.append(Component.empty()
                    .append(Component.newline())
                    .append(participant.displayName())
                    .append(Component.text(": "))
                    .append(Component.text(participant.getScore())
                            .color(NamedTextColor.GOLD)));
        }
        return builder.build();
    }
    
    /**
     * @return a component with all teams and their scores in order from highest to lowest, ties broken alphabetically.
     */
    private Component getAllTeamScores() {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("Team Scores:")
                        .decorate(TextDecoration.BOLD));
        List<Team> sortedTeams = gameManager.getSortedTeams();
        for (Team team : sortedTeams) {
            builder.append(Component.empty()
                    .append(Component.newline())
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD)));
        }
        return builder.build();
    }
    
}
