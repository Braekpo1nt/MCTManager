package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class FirstAsyncThenSyncCommandResult implements AsynchronousCommandResult {
    
    private final @NotNull Main plugin;
    private final @Nullable Component immediateMessage;
    private final @NotNull CompletableFuture<CommandResult> futureResult;
    private final @NotNull Supplier<CommandResult> syncResult;
    
    /**
     * @param plugin the plugin
     * @param firstAsync an async operation to run first
     * @param thenSync a Bukkit-safe sync operation to run after the async is complete
     */
    public FirstAsyncThenSyncCommandResult(@NotNull Main plugin, @Nullable Component immediateMessage, @NotNull CompletableFuture<CommandResult> firstAsync, @NotNull Supplier<CommandResult> thenSync) {
        this.plugin = plugin;
        this.immediateMessage = immediateMessage;
        this.futureResult = firstAsync;
        this.syncResult = thenSync;
    }
    
    @Override
    public @Nullable Component getMessage() {
        return immediateMessage;
    }
    
    @Override
    public void executeAsync(@NotNull CommandSender sender) {
        futureResult
                .thenAccept(asyncResult -> {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        CommandResult.showResult(sender, asyncResult);
                        CommandResult.showResult(sender, syncResult.get());
                    });
                })
        ;
    }
}
