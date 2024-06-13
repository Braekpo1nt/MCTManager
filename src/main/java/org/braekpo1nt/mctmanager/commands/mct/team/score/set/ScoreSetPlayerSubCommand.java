package org.braekpo1nt.mctmanager.commands.mct.team.score.set;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ScoreSetPlayerSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    
    public ScoreSetPlayerSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return CommandResult.failure(getUsage().of("<playerName>").of("<score>"));
        }
        String playerName = args[0];
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        
        if (!gameManager.isParticipant(offlinePlayer.getUniqueId())) {
            if (!gameManager.isOfflineParticipant(offlinePlayer.getUniqueId())) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(playerName)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a participant")));
            } else {
                String teamName = gameManager.getOfflineIGNTeamName(playerName);
                NamedTextColor teamColor = gameManager.getTeamNamedTextColor(teamName);
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Can't change the score of "))
                        .append(Component.text(playerName)
                                .color(teamColor))
                        .append(Component.text(" because they have not logged in since being joined to a team"))
                );
            }
        }
        String scoreString = args[1];
        if (!CommandUtils.isInteger(scoreString)) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text(scoreString)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not an integer")));
        }
        int score = Integer.parseInt(scoreString);
        if (score < 0) {
            return CommandResult.failure(Component.text("Value must be positive"));
        }
        gameManager.setScore(offlinePlayer.getUniqueId(), score);
        int newScore = gameManager.getScore(offlinePlayer.getUniqueId());
        return CommandResult.success(Component.empty()
                .append(Component.text(playerName))
                .append(Component.text(" score is now "))
                .append(Component.text(newScore)));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return CommandUtils.partialMatchParticipantsTabList(gameManager, args[0]);
        }
        return Collections.emptyList();
    }
}
