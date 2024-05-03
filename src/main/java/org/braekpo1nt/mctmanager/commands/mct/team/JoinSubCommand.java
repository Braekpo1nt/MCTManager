package org.braekpo1nt.mctmanager.commands.mct.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JoinSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    
    public JoinSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return CommandResult.failure(getUsage().of("<team>").of("<member>"));
        }
        String teamName = args[0];
        if (!gameManager.hasTeam(teamName)) {
            return CommandResult.failure(Component.text("Team ")
                    .append(Component.text(teamName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" does not exist.")));
        }
        String playerName = args[1];
        Player playerToJoin = Bukkit.getPlayer(playerName);
        if (playerToJoin == null) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Player "))
                    .append(Component.text(playerName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not online.")));
        }
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(teamName);
        if (gameManager.isParticipant(playerToJoin.getUniqueId())) {
            String oldTeamName = gameManager.getTeamName(playerToJoin.getUniqueId());
            if (oldTeamName.equals(teamName)) {
                return CommandResult.success(Component.empty()
                        .append(Component.text(playerName)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is already on team "))
                        .append(formattedTeamDisplayName)
                        .color(NamedTextColor.YELLOW));
            }
        }
        gameManager.joinPlayerToTeam(sender, playerToJoin, teamName);
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return gameManager.getTeamNames().stream().sorted().toList();
        }
        if (args.length == 2) {
            return null;
        }
        return Collections.emptyList();
    }
}
