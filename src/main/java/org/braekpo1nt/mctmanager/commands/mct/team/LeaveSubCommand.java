package org.braekpo1nt.mctmanager.commands.mct.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.participant.OfflineParticipant;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LeaveSubCommand extends TabSubCommand {
    private final Main plugin;
    private final GameManager gameManager;
    
    public LeaveSubCommand(Main plugin, GameManager gameManager, @NotNull String name) {
        super(name);
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1) {
            return CommandResult.failure(getUsage().of("[member]"));
        }
        String playerName;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                return CommandResult.failure("Must be a player to use the no-argument option");
            }
            playerName = player.getName();
        } else {
            playerName = args[0];
        }
        OfflinePlayer playerToLeave = plugin.getServer().getOfflinePlayer(playerName);
        OfflineParticipant offlineParticipant = gameManager.getOfflineParticipant(playerToLeave.getUniqueId());
        if (offlineParticipant == null) {
            return CommandResult.failure(Component.text("Player ")
                    .append(Component.text(playerName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not on a team.")));
        }
        gameManager.leaveParticipant(sender, offlineParticipant);
        return CommandResult.success();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        return CommandUtils.partialMatchTabList(gameManager.getAllParticipantNames(), args[0]);
    }
}
