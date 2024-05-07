package org.braekpo1nt.mctmanager.commands.manager;

import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

public abstract class TabSubCommand extends SubCommand implements TabCompleter {
    public TabSubCommand(@NotNull String name) {
        super(name);
    }
}
