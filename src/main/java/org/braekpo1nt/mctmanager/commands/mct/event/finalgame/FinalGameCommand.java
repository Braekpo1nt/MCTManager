package org.braekpo1nt.mctmanager.commands.mct.event.finalgame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FinalGameCommand extends CommandManager {
    
    public FinalGameCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new TabSubCommand("start") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 2) {
                    return CommandResult.failure(getUsage().of("<first>").of("<second>"));
                }
                String firstTeamId = args[0];
                String secondTeamId = args[1];
                if (firstTeamId.equals(secondTeamId)) {
                    return CommandResult.failure(Component.text("must be two different teams"));
                }
                Team first = gameManager.getTeam(firstTeamId);
                if (first == null) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(firstTeamId)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a valid team name")));
                }
                Team second = gameManager.getTeam(secondTeamId);
                if (second == null) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(secondTeamId)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a valid team name")));
                }
                gameManager.getEventManager().startColossalCombat(sender, first, second);
                return CommandResult.success();
            }
            
            @Override
            public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    return gameManager.getTeamIds().stream().sorted().toList();
                } else if (args.length == 2) {
                    return gameManager.getTeamIds().stream().filter(t -> !t.equals(args[0])).sorted().toList();
                }
                return Collections.emptyList();
            }
        });
        addSubCommand(new SubCommand("stop") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 0) {
                    return CommandResult.failure(getUsage());
                }
                gameManager.getEventManager().stopColossalCombat(sender);
                return CommandResult.success();
            }
        });
    }
}
