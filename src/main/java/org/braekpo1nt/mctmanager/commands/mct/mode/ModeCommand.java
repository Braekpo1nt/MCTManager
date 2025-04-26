package org.braekpo1nt.mctmanager.commands.mct.mode;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class ModeCommand extends CommandManager {
    public ModeCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new MaintenanceSubCommand(gameManager, "maintenance"));
    }
}
