package org.braekpo1nt.mctmanager.commands.mct.debug;

import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.mct.debug.log.LogSubCommand;
import org.jetbrains.annotations.NotNull;

public class DebugCommand extends CommandManager {
    public DebugCommand(@NotNull String name) {
        super(name);
        addSubCommand(new LogSubCommand("log"));
    }
}
