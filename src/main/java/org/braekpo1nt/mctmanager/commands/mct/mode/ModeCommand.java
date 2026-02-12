package org.braekpo1nt.mctmanager.commands.mct.mode;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.Mode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ModeCommand extends CommandManager {
    public ModeCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new SubCommand(Mode.MAINTENANCE.getName()) {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return gameManager.switchMode(Mode.MAINTENANCE);
            }
        });
        addSubCommand(new SubCommand(Mode.PRACTICE.getName()) {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return gameManager.switchMode(Mode.PRACTICE);
            }
        });
        addSubCommand(new SubCommand(Mode.EVENT.getName()) {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return gameManager.switchMode(Mode.EVENT);
            }
        });
    }
}
