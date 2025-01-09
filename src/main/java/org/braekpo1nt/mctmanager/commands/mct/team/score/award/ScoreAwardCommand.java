package org.braekpo1nt.mctmanager.commands.mct.team.score.award;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ScoreAwardCommand extends CommandManager {
    public ScoreAwardCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new TabSubCommand("player") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 2) {
                    return CommandResult.failure(getUsage().of("<playerName>").of("<score>"));
                }
                String playerName = args[0];
                Player participant = Bukkit.getPlayer(playerName);
                if (participant == null) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(playerName))
                            .append(Component.text(" is not online.")));
                }
                if (!gameManager.isParticipant(participant.getUniqueId())) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(playerName)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a participant")));
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
                gameManager.awardPointsToParticipant(participant, score);
                return CommandResult.success();
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    return null;
                }
                return Collections.emptyList();
            }
        });
    }
}
