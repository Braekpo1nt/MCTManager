package org.braekpo1nt.mctmanager.commands.mct.team;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class JoinSubCommand extends TabSubCommand {
    private final Main plugin;
    private final GameManager gameManager;
    
    public JoinSubCommand(Main plugin, GameManager gameManager, @NotNull String name) {
        super(name);
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return CommandResult.failure(getUsage().of("<team>").of("[member]"));
        }
        String teamId = args[0];
        String playerName;
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                return CommandResult.failure("Must be a player to use the no-argument option");
            }
            playerName = player.getName();
        } else {
            playerName = args[1];
        }
        return GameManagerUtils.joinParticipant(sender, plugin, gameManager, playerName, teamId);
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return gameManager.getTeamIds().stream().sorted().toList();
        }
        if (args.length == 2) {
            // this is intentional to allow default auto-completing of online players
            return null;
        }
        return Collections.emptyList();
    }
}
