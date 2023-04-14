package org.braekpo1nt.mctmanager.commands.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FinalGameSubCommand implements TabExecutor {
    private final GameManager gameManager;

    public FinalGameSubCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /mct game finalgame <teamA> <teamB>")
                    .color(NamedTextColor.RED));
            return true;
        }
        String teamA = args[0];
        String teamB = args[1];
        if (teamA.equals(teamB)) {
            sender.sendMessage(Component.text("must be two different teams")
                    .color(NamedTextColor.RED));
            return true;
        }
        if (!gameManager.hasTeam(teamA)) {
            sender.sendMessage(Component.empty()
                            .append(Component.text(teamA)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a valid team name"))
                            .color(NamedTextColor.RED));
            return true;
        }
        if (!gameManager.hasTeam(teamB)) {
            sender.sendMessage(Component.empty()
                    .append(Component.text(teamB)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not a valid team name"))
                    .color(NamedTextColor.RED));
            return true;
        }
        gameManager.setFinalGameTeams(teamA, teamB);
        gameManager.startGame(MCTGames.FINAL_GAME, sender);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return gameManager.getTeamNames().stream().sorted().toList();
    }
}
