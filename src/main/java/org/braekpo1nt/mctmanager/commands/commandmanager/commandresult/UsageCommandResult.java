package org.braekpo1nt.mctmanager.commands.commandmanager.commandresult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UsageCommandResult implements CommandResult {
    private final @NotNull List<@NotNull String> args;
    
    public UsageCommandResult(@NotNull String firstArg) {
        args = new ArrayList<>(List.of(firstArg));
    }
    
    /**
     * Append a downstream argument to this {@link UsageCommandResult}
     * @param arg the downstream argument to append
     * @return this {@link UsageCommandResult} for easy reuse
     */
    public UsageCommandResult with(@NotNull String arg) {
        args.add(arg);
        return this;
    }
    
    /**
     * Appends all downstream arguments of the given {@link UsageCommandResult} to this one
     * @param usage the {@link UsageCommandResult} to append to this one. Contains all downstream arguments to append.
     * @return this {@link UsageCommandResult} for easy reuse
     */
    public UsageCommandResult with(@NotNull UsageCommandResult usage) {
        args.addAll(usage.args);
        return this;
    }
    
    public UsageCommandResult with(String arg, TextDecoration decoration) {
        //TODO: Implement with decoration
        return with(arg);
    }
    
    /**
     * Usage messages look like "Usage: /arg1 arg2 ..." and are colored red.
     * @return the usage message
     */
    @Override
    public Component getMessage() {
        //TODO: Use Component.text() with no args to get a builder
        return Component.text("Usage: /")
                .append(Component.text(String.join(" ", args)))
                .color(NamedTextColor.RED);
    }
    
    @Override
    public @NotNull CommandResult and(CommandResult other) {
        return new CompositeCommandResult(this, other);
    }
}
