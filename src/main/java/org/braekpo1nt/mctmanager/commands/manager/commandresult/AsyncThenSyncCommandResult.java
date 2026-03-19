package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class AsyncThenSyncCommandResult extends AsyncCommandResult {
    
    /**
     * the operation to be executed on the main bukkit thread after the asyncSupplier has finished,
     * and the result of which will be shown to the command executor upon completion
     */
    private final @NotNull AsyncCommandResult.ResultSupplier syncSupplier;
    
    /**
     * @param plugin The plugin to use for the asynchronous operation
     * @param immediateMessage the message to send immediately, before the asynchronous operation is complete. Null if
     * no such message needs to be sent to the command executor.
     * @param asyncSupplier the operation to be executed on an asynchronous thread, and the result of which will be
     * shown to the command executor upon completion
     * @param syncSupplier the operation to be executed on the main bukkit thread after the asyncSupplier has finished,
     * and the result of which will be shown to the command executor upon completion
     */
    public AsyncThenSyncCommandResult(
            @NotNull Main plugin,
            @Nullable Component immediateMessage,
            @NotNull AsyncCommandResult.ResultSupplier asyncSupplier,
            @NotNull AsyncCommandResult.ResultSupplier syncSupplier
    ) {
        super(plugin, immediateMessage, asyncSupplier);
        this.syncSupplier = syncSupplier;
    }
    
    /**
     * Execute the {@link #asyncSupplier} in an asynchronous thread, and show the resulting {@link CommandResult} to the
     * given sender back on the main thread.
     * @param sender the sender to see the message resulting from the asynchronous operation {@link #asyncSupplier}
     */
    @Override
    public void executeAsync(@NotNull CommandSender sender) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CommandResult asyncResult;
            try {
                asyncResult = asyncSupplier.run();
            } catch (CommandSyntaxException e) {
                asyncResult = CommandResult.failure(Component.empty()
                        .append(Component.text(e.getMessage()))
                );
            } catch (Exception e) {
                Main.logger().log(Level.SEVERE, "An error occurred executing an asynchronous command", e);
                asyncResult = CommandResult.failure(Component.empty()
                        .append(Component.text("An error occurred executing an asynchronous command. See console for details:"))
                        .append(Component.newline())
                        .append(Component.text(e.getMessage()))
                        .color(NamedTextColor.RED)
                );
            }
            // final or effectively final for runTask
            final CommandResult finalAsyncResult = asyncResult;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                CommandResult.showResult(sender, finalAsyncResult);
                CommandResult syncResult;
                try {
                    syncResult = syncSupplier.run();
                } catch (CommandSyntaxException e) {
                    syncResult = CommandResult.failure(Component.empty()
                            .append(Component.text(e.getMessage()))
                    );
                } catch (Exception e) {
                    Main.logger().log(Level.SEVERE, "An error occurred executing an asynchronous command", e);
                    syncResult = CommandResult.failure(Component.empty()
                            .append(Component.text("An error occurred executing an asynchronous command. See console for details:"))
                            .append(Component.newline())
                            .append(Component.text(e.getMessage()))
                            .color(NamedTextColor.RED)
                    );
                }
                CommandResult.showResult(sender, syncResult);
            });
        });
    }
}
