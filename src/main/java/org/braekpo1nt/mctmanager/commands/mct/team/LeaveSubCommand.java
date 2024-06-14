package org.braekpo1nt.mctmanager.commands.mct.team;

import net.kyori.adventure.text.Component;
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

public class LeaveSubCommand extends TabSubCommand {
    private final GameManager gameManager;
    
    public LeaveSubCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return CommandResult.failure(getUsage().of("<member>"));
        }
        String playerName = args[0];
        OfflinePlayer playerToLeave = Bukkit.getOfflinePlayer(playerName);
        if (!gameManager.isParticipant(playerToLeave.getUniqueId())) {
            if (!gameManager.isOfflineParticipant(playerToLeave.getUniqueId())) {
                return CommandResult.failure(Component.text("Player ")
                        .append(Component.text(playerName)
                                .decorate(TextDecoration.BOLD))
                        .append(Component.text(" is not on a team.")));
            }
        }
        gameManager.leavePlayer(sender, playerToLeave, playerName);
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return CommandUtils.partialMatchParticipantsTabList(gameManager, args[0]);
    }
}
