package org.braekpo1nt.mctmanager.commands.mct.mode;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ModeCommand extends CommandManager {
    public ModeCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new SubCommand("maintenance") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return gameManager.switchMode("maintenance");
            }
        });
        addSubCommand(new SubCommand("practice") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return gameManager.switchMode("practice");
            }
        });
        addSubCommand(new SubCommand("event") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return gameManager.switchMode("event");
            }
        });
    }
}
