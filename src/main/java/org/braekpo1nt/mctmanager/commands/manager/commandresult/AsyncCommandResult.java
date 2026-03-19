package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Allows you to send a result message back to the command executor
 * after an asynchronous operation
 */
public class AsyncCommandResult implements AsynchronousCommandResult {
    
    @FunctionalInterface
    public interface ResultSupplier {
        @NotNull CommandResult run() throws CommandSyntaxException;
    }
    
    /**
     * The plugin to use for the asynchronous operation
     */
    protected final Main plugin;
    /**
     * the message to send immediately, before the asynchronous operation is complete.
     * Null if no such message needs to be sent to the command executor.
     */
    protected final @Nullable Component immediateMessage;
    /**
     * the operation to be executed on an asynchronous thread,
     * and the result of which will be shown to
     */
    protected final @NotNull AsyncCommandResult.ResultSupplier asyncSupplier;
    
    /**
     * @param plugin The plugin to use for the asynchronous operation
     * @param immediateMessage the message to send immediately, before the asynchronous operation is complete. Null if
     * no such message needs to be sent to the command executor.
     * @param supplier the operation to be executed on an asynchronous thread, and the result of which will be shown to
     * the command executor upon completion
     */
    public AsyncCommandResult(
            @NotNull Main plugin,
            @Nullable Component immediateMessage,
            @NotNull AsyncCommandResult.ResultSupplier supplier
    ) {
        this.plugin = plugin;
        this.immediateMessage = immediateMessage;
        this.asyncSupplier = supplier;
    }
    
    /**
     * @return the {@link #immediateMessage} only
     */
    @Override
    public @Nullable Component getMessage() {
        return immediateMessage;
    }
    
    /**
     * Execute the {@link #asyncSupplier} in an asynchronous thread, and show the resulting {@link CommandResult} to the
     * given sender back on the main thread.
     * @param sender the sender to see the message resulting from the asynchronous operation {@link #asyncSupplier}
     */
    @Override
    public void executeAsync(@NotNull CommandSender sender) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CommandResult result;
            try {
                result = asyncSupplier.run();
            } catch (CommandSyntaxException e) {
                result = CommandResult.failure(Component.empty()
                        .append(Component.text(e.getMessage()))
                );
            } catch (Exception e) {
                Main.logger().log(Level.SEVERE, "An error occurred executing an asynchronous command", e);
                result = CommandResult.failure(Component.empty()
                        .append(Component.text("An error occurred executing an asynchronous command. See console for details:"))
                        .append(Component.newline())
                        .append(Component.text(e.getMessage()))
                        .color(NamedTextColor.RED)
                );
            }
            // final or effectively final for runTask
            final CommandResult commandResult = result;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                CommandResult.showResult(sender, commandResult);
            });
        });
    }
    
    /**
     * Not supported for {@link AsyncCommandResult}
     * @throws UnsupportedOperationException when called, because this is not supported yet
     */
    @Override
    public @NotNull CommandResult and(CommandResult other) {
        // TODO: find a way to make this work
        throw new UnsupportedOperationException("Cannot add an AsyncCommandResult to a CompositeCommandResult");
    }
}
