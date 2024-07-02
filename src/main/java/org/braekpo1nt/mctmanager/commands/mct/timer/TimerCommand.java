package org.braekpo1nt.mctmanager.commands.mct.timer;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TimerCommand extends CommandManager {
    public TimerCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new SubCommand("pause") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.getTimerManager().pause();
                return CommandResult.success();
            }
        });
        addSubCommand(new SubCommand("resume") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.getTimerManager().resume();
                return CommandResult.success();
            }
        });
        addSubCommand(new SubCommand("skip") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.getTimerManager().skip();
                return CommandResult.success();
            }
        });
    }
}
