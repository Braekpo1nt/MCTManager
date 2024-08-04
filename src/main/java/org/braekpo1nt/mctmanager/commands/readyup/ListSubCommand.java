package org.braekpo1nt.mctmanager.commands.readyup;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.ListType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ListSubCommand extends CommandManager {
    
    public ListSubCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new SubCommand("players") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.getEventManager().listReady(sender, ListType.PARTICIPANTS);
                return CommandResult.success();
            }
        });
        addSubCommand(new SubCommand("teams") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.getEventManager().listReady(sender, ListType.TEAMS);
                return CommandResult.success();
            }
        });
    }
}
