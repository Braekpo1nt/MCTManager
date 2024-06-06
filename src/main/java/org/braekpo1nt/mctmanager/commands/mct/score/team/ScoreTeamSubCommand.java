package org.braekpo1nt.mctmanager.commands.mct.score.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ScoreTeamSubCommand extends TabSubCommand {
    
    private final GameManager gameManager;
    
    public ScoreTeamSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            return CommandResult.failure(getUsage().of("[<teamId>]"));
        }
        String team;
        if (args.length == 0) {
            if (!(sender instanceof Player participant)) {
                return CommandResult.failure(getUsage().of("<teamId>"));
            }
            if (!gameManager.isParticipant(participant.getUniqueId())) {
                return CommandResult.failure(Component.text("You are not a participant"));
            }
            team = gameManager.getTeamName(participant.getUniqueId());
        } else {
            team = args[0];
            if (!gameManager.hasTeam(team)) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text(team)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a valid team ID")));
            }
        }
        
        Component displayName = gameManager.getFormattedTeamDisplayName(team);
        int score = gameManager.getScore(team);
        return CommandResult.success(Component.empty()
                .append(displayName)
                .append(Component.text(": "))
                .append(Component.text(score)
                        .color(NamedTextColor.GOLD)));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return gameManager.getAllTeamNames();
    }
}
