package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
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
    default @NotNull CommandResult and(CommandResult other) {
        return new CompositeCommandResult(this, other);
    }
    
    static <T extends Audience> void showResult(@NotNull Collection<T> senders, @NotNull CommandResult commandResult) {
        Component message = commandResult.getMessage();
        if (message != null) {
            Audience.audience(senders).sendMessage(message);
        }
    }
    
    /**
     * Convenience method for showing the message from a given {@link CommandResult}
     * @param sender the sender to show the result to
     * @param commandResult the {@link CommandResult} to get the {@link CommandResult#getMessage()} from
     */
    static void showResult(@NotNull Audience sender, @NotNull CommandResult commandResult) {
        Component message = commandResult.getMessage();
        if (message != null) {
            sender.sendMessage(message);
        }
    }
    
    static void showResult(@NotNull Audience sender, @NotNull CompletableFuture<CommandResult> completableFuture) {
        completableFuture
                .thenAccept(asyncResult -> showResult(sender, asyncResult));
    }
    
    static <T extends Audience> void showResult(@NotNull Collection<T> senders, @NotNull CompletableFuture<CommandResult> completableFuture) {
        completableFuture
                .thenAccept(asyncResult -> showResult(senders, asyncResult));
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
     * For convenience, if you want to return a simple type where a {@link CompletableFuture<CommandResult>}
     * is expected
     * @return this CommandResult as a {@link CompletableFuture<CommandResult>}
     */
    default CompletableFuture<CommandResult> asFuture() {
        return CompletableFuture.completedFuture(this);
    }
    
    /**
     * Convenience method to report that a SQLException occurred when running a command.
     * Also logs the error to the console.
     * @param tryingTo the attempted action, complete the sentence "A database error occurred trying to..." (no
     * trailing or leading spaces needed)
     * @param e the exception that occurred
     * @return a {@link CommandResult} detailing the database error that occurred
     */
    static @NotNull CommandResult sqlException(String tryingTo, SQLException e) {
        Main.logger().log(Level.SEVERE, String.format("A database error occurred trying to %s", tryingTo), e);
        return failure(Component.empty()
                .append(Component.text("A database error occurred trying to "))
                .append(Component.text(tryingTo))
                .append(Component.text(". See console for details"))
                .append(Component.newline())
                .append(Component.text(e.getMessage()))
        );
    }
    
    /**
     * Convenience method to report that a {@link Throwable} occurred when running a command.
     * Also logs the error to the console.
     * @param tryingTo the attempted action, complete the sentence "An error occurred trying to..." (no
     * trailing or leading spaces needed)
     * @param e the throwable that occurred
     * @return a {@link CommandResult} detailing the error that occurred
     */
    static @NotNull CommandResult throwable(String tryingTo, Throwable e) {
        Main.logger().log(Level.SEVERE, String.format("An error occurred trying to %s", tryingTo), e);
        return failure(Component.empty()
                .append(Component.text("An error occurred trying to "))
                .append(Component.text(tryingTo))
                .append(Component.text(". See console for details"))
                .append(Component.newline())
                .append(Component.text(e.getMessage()))
        );
    }
    
    /**
     * Convenience method to add a link in a chain of {@link CompletableFuture<CommandResult>}s
     * which adds on a new result and passes the combined results to the next link in the chain.
     * @param chain the previous {@link CompletableFuture<CommandResult>}
     * @param operation the next {@link CompletableFuture<CommandResult>}
     * @param executor the thread to execute on
     * @param <T> a CommandResult implementation
     * @return a new {@link CompletableFuture<CommandResult>} with the operation's result appended to
     * the chain's result using {@link CommandResult#and(CommandResult)}
     */
    static <T extends CommandResult> CompletableFuture<CommandResult> appendAsync(
            CompletableFuture<T> chain,
            Supplier<CompletableFuture<T>> operation,
            Executor executor
    ) {
        return chain.thenComposeAsync(
                result -> operation.get().thenApply(result::and),
                executor
        );
    }
    
}
