package org.braekpo1nt.mctmanager.commands.commandmanager.commandresult;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.commandmanager.Usage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandResult {
    
    /**
     * @return the message to send to the command sender(s)
     */
    @Nullable Component getMessage();
    
    /**
     * Concatenates an additional {@link CommandResult} after the current one.
     * @param other the additional {@link CommandResult} to concatenate to this one.
     * @return the combined {@link CommandResult}s
     */
    @NotNull CommandResult and(CommandResult other);
    
    /**
     * Indicates a successful use of a command.
     * @return a new {@link SuccessCommandResult}
     */
    static CommandResult success() {
        return new SuccessCommandResult(null);
    }
    
    /**
     * Indicates a successful use of a command, with an optional message for the sender.
     * @param message the message to give the sender, if desired.
     * @return a new {@link SuccessCommandResult}
     */
    static CommandResult success(@Nullable Component message) {
        return new SuccessCommandResult(message);
    }
    
    /**
     * Indicates a failed use of a command, with an optional message for the sender.
     * @param message a message indicating the nature of the failure.
     * @return a new {@link FailureCommandResult}
     */
    static CommandResult failure(@Nullable Component message) {
        return new FailureCommandResult(message);
    }
    
    /**
     * A convenience overload of {@link CommandResult#failure(Component)} where the message is a plain string.
     * @param message a message indicating the nature of the failure. Can't be null.
     * @return a new {@link FailureCommandResult}
     * @see CommandResult#failure(Component)
     */
    static CommandResult failure(@NotNull String message) {
        return new FailureCommandResult(Component.text(message));
    }
    
    /**
     * An overload of {@link CommandResult#failure(Component)} where the message is a {@link Usage} object.
     * @param usage the {@link Usage} object describing the proper usage of the failed command
     * @return a new {@link UsageCommandResult}
     * @see CommandResult#failure(Component) 
     */
    static CommandResult failure(@NotNull Usage usage) {
        return new UsageCommandResult(usage);
    }
}
