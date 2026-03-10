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

import java.util.logging.Level;

public class BrigadierAdapters {
    @FunctionalInterface
    public interface ResultCommand {
        @NotNull CommandResult run(@NotNull CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
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
            CommandSourceStack source = ctx.getSource();
            CommandSender sender = source.getSender();
            
            try {
                CommandResult result = command.run(ctx);
                CommandResult.showResult(sender, result);
                return Command.SINGLE_SUCCESS;
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
