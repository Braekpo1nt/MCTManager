package org.braekpo1nt.mctmanager.commands.commandmanager.commandresult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.Usage;
import org.jetbrains.annotations.NotNull;

public class UsageCommandResult implements CommandResult {
    private final @NotNull Usage usage;
    
    public UsageCommandResult(@NotNull Usage usage) {
        this.usage = usage;
    }
    
    public UsageCommandResult(@NotNull String firstArg) {
        usage = new Usage(firstArg);
    }
    
    /**
     * Append a downstream argument to this {@link UsageCommandResult}
     * @param arg the downstream argument to append
     * @return this {@link UsageCommandResult} for easy reuse
     */
    public UsageCommandResult of(@NotNull String arg) {
        usage.of(arg);
        return this;
    }
    
    /**
     * Appends all downstream arguments of the given {@link UsageCommandResult} to this one
     * @param usageResult the {@link UsageCommandResult} to append to this one. Contains all downstream arguments to append.
     * @return this {@link UsageCommandResult} for easy reuse
     */
    public UsageCommandResult of(@NotNull UsageCommandResult usageResult) {
        this.usage.of(usageResult.usage);
        return this;
    }
    
    public UsageCommandResult of(@NotNull String arg, @NotNull TextDecoration decoration) {
        this.usage.of(arg, decoration);
        return this;
    }
    
    /**
     * Usage messages look like "Usage: /arg1 arg2 ..." and are colored red.
     * @return the usage message
     */
    @Override
    public Component getMessage() {
        return usage.toComponent().color(NamedTextColor.RED);
    }
    
    @Override
    public @NotNull CommandResult and(CommandResult other) {
        return new CompositeCommandResult(this, other);
    }
}
