package org.braekpo1nt.mctmanager.commands.manager.commandresult;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.logging.Level;

public class AsyncCommandResult implements CommandResult {
    
    private final Main plugin;
    private final @Nullable Component immediateMessage;
    private final Supplier<CommandResult> supplier;
    
    public AsyncCommandResult(
            @NotNull Main plugin,
            @Nullable Component immediateMessage,
            @NotNull Supplier<CommandResult> supplier
    ) {
        this.plugin = plugin;
        this.immediateMessage = immediateMessage;
        this.supplier = supplier;
    }
    
    @Override
    public @Nullable Component getMessage() {
        return immediateMessage;
    }
    
    public void executeAsync(@NotNull CommandSender sender) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CommandResult result;
            try {
                result = supplier.get();
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
                Component message = commandResult.getMessage();
                if (message != null) {
                    sender.sendMessage(message);
                }
            });
        });
    }
    
    @Override
    public @NotNull CommandResult and(CommandResult other) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
