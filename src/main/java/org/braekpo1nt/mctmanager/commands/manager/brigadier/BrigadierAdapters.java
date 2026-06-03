package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BrigadierAdapters {
    @FunctionalInterface
    public interface ResultCommand {
        @NotNull CommandResult run(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }
    
    @FunctionalInterface
    public interface FutureResultCommand {
        @NotNull CompletableFuture<CommandResult> run(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }
    
    /**
     * @param command an implementation (usually a lambda or method reference) of {@link ResultCommand}.
     * This can throw a {@link CommandSyntaxException} and it will be re-thrown for handling by
     * the normal command lifecycle. Any other exception types will be caught, reported to the sender,
     * and logged to the console.
     * @return an implementation of a {@link Command<CommandSourceStack>} which calls the given
     * ResultCommand and sends the {@link CommandResult} from {@link ResultCommand#run(CommandContext)}
     * to the {@link CommandSourceStack#getSender()}.
     */
    public static @NotNull Command<CommandSourceStack> wraps(@NotNull ResultCommand command) {
        return ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            
            try {
                CommandResult result = command.run(ctx);
                CommandResult.showResult(sender, result);
                return Command.SINGLE_SUCCESS;
            } catch (CommandSyntaxException e) {
                throw e;
            } catch (Exception e) {
                String message = e.getMessage();
                sender.sendMessage(Component.empty()
                        .append(Component.text("An internal error occurred. See console for details."))
                        .append(Component.newline())
                        .append(Component.text(message != null ? message : "unknown"))
                        .color(NamedTextColor.RED)
                );
                Main.logger().log(Level.SEVERE, "An error occurred executing this command.", e);
                return 0;
            }
        };
    }
    
    public static @NotNull Command<CommandSourceStack> wrapsFuture(@NotNull FutureResultCommand command) {
        return ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            try {
                CompletableFuture<CommandResult> futureResult = command.run(ctx)
                        // below is to catch exceptions while the future is being run,
                        // not while it is being built
                        .exceptionally(ex -> switch (ex) {
                            case CommandSyntaxException c -> CommandResult.failure(c.getMessage());
                            case SQLException s -> CommandResult.sqlException("executing command", s);
                            default -> {
                                Main.logger().log(Level.SEVERE, "An error occurred executing this command.", ex);
                                yield CommandResult.failure(
                                        Component.empty()
                                                .append(Component.text("An internal error occurred. See console for details."))
                                                .append(Component.newline())
                                                .append(Component.text(ex.getMessage()))
                                                .color(NamedTextColor.RED)
                                );
                            }
                        });
                CommandResult.showResult(sender, futureResult);
                return Command.SINGLE_SUCCESS;
                // below is to catch exceptions in command.run() before or while the future is being built,
                // not for catching exceptions inside the future
            } catch (CommandSyntaxException e) {
                throw e;
            } catch (Exception e) {
                sender.sendMessage(Component.empty()
                        .append(Component.text("An internal error occurred. See console for details."))
                        .append(Component.newline())
                        .append(Component.text(e.getMessage()))
                        .color(NamedTextColor.RED)
                );
                Main.logger().log(Level.SEVERE, "An error occurred executing this command.", e);
                return 0;
            }
        };
    }
}
