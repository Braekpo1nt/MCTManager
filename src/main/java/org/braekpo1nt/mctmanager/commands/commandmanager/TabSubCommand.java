package org.braekpo1nt.mctmanager.commands.commandmanager;

import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

public abstract class TabSubCommand implements SubCommand, TabCompleter {
    protected final @NotNull String name;
    
    public TabSubCommand(@NotNull String name) {
        this.name = name;
    }
    
    @Override
    public @NotNull String getName() {
        return name;
    }
}
