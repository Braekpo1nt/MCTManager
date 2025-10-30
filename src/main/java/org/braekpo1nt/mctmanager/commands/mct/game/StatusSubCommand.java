package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Handles showing status of participants in active games, particularly who hasn't completed the footrace
 */
public class StatusSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public StatusSubCommand(Main plugin, GameManager gameManager, String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return CommandResult.failure(Component.text("Only players can use this command").color(NamedTextColor.RED));
        }
        if (!gameManager.isAdmin(player.getUniqueId())) {
            return CommandResult.failure(Component.text("Only admins can use this command").color(NamedTextColor.RED));
        }
        List<GameInstanceId> activeGameIds = gameManager.getActiveGameIds();
        if (activeGameIds.isEmpty()) {
            return CommandResult.failure(Component.text("No games are currently active").color(NamedTextColor.RED));
        }
        List<GameInstanceId> footraceGames = activeGameIds.stream()
                .filter(id -> id.getGameType() == GameType.FOOT_RACE)
                .toList();
        if (footraceGames.isEmpty()) {
            return CommandResult.failure(Component.text("No footrace games are currently active").color(NamedTextColor.YELLOW));
        }
        var resultBuilder = Component.text()
                .append(Component.text("=== Footrace Game Status ===").color(NamedTextColor.GOLD))
                .append(Component.newline());
        for (GameInstanceId gameId : footraceGames) {
            MCTGame game = gameManager.getActiveGame(gameId);
            if (!(game instanceof FootRaceGame footRaceGame)) {
                continue;
            }
            resultBuilder.append(Component.text("Game: ").color(NamedTextColor.AQUA))
                    .append(Component.text(gameId.getTitle()).color(NamedTextColor.WHITE))
                    .append(Component.text(" (").color(NamedTextColor.GRAY))
                    .append(Component.text(gameId.getConfigFile()).color(NamedTextColor.GRAY))
                    .append(Component.text(")").color(NamedTextColor.GRAY))
                    .append(Component.newline());
            Map<UUID, FootRaceParticipant> participants = footRaceGame.getParticipants();
            if (participants.isEmpty()) {
                resultBuilder.append(Component.text("  No participants in this game").color(NamedTextColor.GRAY))
                        .append(Component.newline());
                continue;
            }
            List<FootRaceParticipant> incompleteParticipants = participants.values().stream()
                    .filter(participant -> !participant.isFinished())
                    .sorted(Comparator.comparing(p -> p.getPlayer().getName()))
                    .toList();
            List<FootRaceParticipant> completeParticipants = participants.values().stream()
                    .filter(FootRaceParticipant::isFinished)
                    .sorted(Comparator.comparing(FootRaceParticipant::getPlacement))
                    .toList();
            resultBuilder.append(Component.text("  Total Participants: ").color(NamedTextColor.WHITE))
                    .append(Component.text(participants.size()).color(NamedTextColor.YELLOW))
                    .append(Component.newline());
            resultBuilder.append(Component.text("  Completed: ").color(NamedTextColor.GREEN))
                    .append(Component.text(completeParticipants.size()).color(NamedTextColor.WHITE))
                    .append(Component.newline());
            resultBuilder.append(Component.text("  Still Racing: ").color(NamedTextColor.RED))
                    .append(Component.text(incompleteParticipants.size()).color(NamedTextColor.WHITE))
                    .append(Component.newline());
            if (!incompleteParticipants.isEmpty()) {
                resultBuilder.append(Component.text("  Players Still Racing:").color(NamedTextColor.RED))
                        .append(Component.newline());
                for (FootRaceParticipant participant : incompleteParticipants) {
                    String teamName = participant.getTeamId() != null ? 
                            participant.getTeamId() : "No Team";
                    resultBuilder.append(Component.text("    - ").color(NamedTextColor.GRAY))
                            .append(Component.text(participant.getPlayer().getName()).color(NamedTextColor.WHITE))
                            .append(Component.text(" (").color(NamedTextColor.GRAY))
                            .append(Component.text(teamName).color(NamedTextColor.AQUA))
                            .append(Component.text(")").color(NamedTextColor.GRAY))
                            .append(Component.newline());
                }
            } else {
                resultBuilder.append(Component.text("  All participants have finished!").color(NamedTextColor.GREEN))
                        .append(Component.newline());
            }
            resultBuilder.append(Component.newline());
        }
        return CommandResult.success(resultBuilder.build());
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
