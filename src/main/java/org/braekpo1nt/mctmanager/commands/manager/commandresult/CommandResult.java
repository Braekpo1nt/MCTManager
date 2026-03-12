package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.logging.Level;

public interface CommandResult {
    
    /**
     * @return the message to send to the command sender(s)
     */
    @Nullable Component getMessage();
    
    /**
     * @return the message to send to the command sender(s), or empty if
     * the message is null
     * @see #getMessage()
     */
    default @NotNull Component getMessageOrEmpty() {
        Component message = getMessage();
        return message != null ? message : Component.empty();
    }
    
    /**
     * Concatenates an additional {@link CommandResult} after the current one.
     * @param other the additional {@link CommandResult} to concatenate to this one.
     * @return the combined {@link CommandResult}s
     */
    @NotNull CommandResult and(CommandResult other);
    
    /**
     * Convenience method for showing the message from a given {@link CommandResult}
     * @param sender the sender to show the result to
     * @param commandResult the {@link CommandResult} to get the {@link CommandResult#getMessage()} from
     */
    static void showResult(@NotNull CommandSender sender, CommandResult commandResult) {
        Component message = commandResult.getMessage();
        if (message != null) {
            sender.sendMessage(message);
        }
        if (commandResult instanceof AsyncCommandResult asyncResult) {
            asyncResult.executeAsync(sender);
        }
    }
    
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
    
    /**
     * @param plugin The plugin to use for the asynchronous operation
     * @param immediateMessage the message to send immediately, before the asynchronous operation is complete. Null if
     * no such message needs to be sent to the command executor.
     * @param supplier the operation to be executed on an asynchronous thread, and the result of which will be shown to
     * the command executor upon completion
     */
    static CommandResult async(@NotNull Main plugin, Component immediateMessage, AsyncCommandResult.ResultSupplier supplier) {
        return new AsyncCommandResult(plugin, immediateMessage, supplier);
    }
    
    /**
     * Convenience method to report that a SQLException occurred when running a command.
     * Also logs the error to the console.
     * @param attemptedAction the attempted action, complete the sentence "A database error occurred trying to..." (no
     * trailing or leading spaces needed)
     * @param e the exception that occurred
     * @return a {@link CommandResult} detailing the database error that occurred and
     */
    static @NotNull CommandResult sqlException(String attemptedAction, SQLException e) {
        Main.logger().log(Level.SEVERE, String.format("A database error occurred trying to %s", attemptedAction), e);
        return failure(Component.empty()
                .append(Component.text("A database error occurred. See console for details."))
                .append(Component.newline())
                .append(Component.text(e.getMessage()))
        );
    }
}
