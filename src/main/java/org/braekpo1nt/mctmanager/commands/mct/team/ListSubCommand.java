package org.braekpo1nt.mctmanager.commands.mct.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    
    public ListSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            return CommandResult.failure(getUsage().of("[true|false]"));
        }
        Component teamDisplay = getTeamDisplay(sender);
        if (args.length == 0) {
            sender.sendMessage(teamDisplay);
            return CommandResult.success();
        }
        String displayToAll = args[0];
        switch (displayToAll) {
            case "true" -> {
                Bukkit.getServer().sendMessage(teamDisplay);
            }
            case "false" -> {
                sender.sendMessage(teamDisplay);
            }
            default -> {
                sender.sendMessage(Component.empty()
                        .append(Component.text(displayToAll)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not a recognized option"))
                        .color(NamedTextColor.RED));
            }
        }

        return CommandResult.success();
    }
    
    private Component getTeamDisplay(CommandSender sender) {
        TextComponent.Builder messageBuilder = Component.text().append(Component.text("TEAMS\n")
                    .decorate(TextDecoration.BOLD));
        List<OfflinePlayer> offlinePlayers = gameManager.getOfflineParticipants();
        List<String> teamNames = gameManager.getTeamNames().stream().toList();
        
        for (String teamName : teamNames) {
            int teamScore = gameManager.getScore(teamName);
            NamedTextColor teamNamedTextColor = gameManager.getTeamNamedTextColor(teamName);
            messageBuilder.append(Component.empty()
                            .append(gameManager.getFormattedTeamDisplayName(teamName))
                            .append(Component.text(" - "))
                            .append(Component.text(teamScore)
                                    .decorate(TextDecoration.BOLD)
                                    .color(NamedTextColor.GOLD))
                    .append(Component.text(":\n")));
            for (OfflinePlayer offlinePlayer : offlinePlayers) {
                String playerTeam = gameManager.getTeamName(offlinePlayer.getUniqueId());
                int playerScore = gameManager.getScore(offlinePlayer.getUniqueId());
                if (offlinePlayer.getName() == null) {
                    continue;
                }
                if (playerTeam.equals(teamName)) {
                    messageBuilder.append(Component.empty()
                            .append(Component.text("  "))
                            .append(Component.text(offlinePlayer.getName())
                                    .color(teamNamedTextColor))
                            .append(Component.text(" - "))
                            .append(Component.text(playerScore)
                                    .decorate(TextDecoration.BOLD)
                                    .color(NamedTextColor.GOLD))
                            .append(Component.text("\n")));
                }
            }
        }
        
        return messageBuilder.build();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("true", "false");
        }
        return Collections.emptyList();
    }
}
