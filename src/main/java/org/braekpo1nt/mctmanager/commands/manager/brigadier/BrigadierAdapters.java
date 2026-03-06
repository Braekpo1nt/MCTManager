package org.braekpo1nt.mctmanager.commands.manager.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.PermissionedLiteralArgumentBuilder;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class BrigadierAdapters {
    @FunctionalInterface
    public interface ResultCommand {
        @NotNull CommandResult run(@NotNull CommandContext<CommandSourceStack> ctx) throws Exception;
    }
    
    public static @NotNull Command<CommandSourceStack> wraps(@NotNull ResultCommand command) {
        return ctx -> {
            CommandSourceStack source = ctx.getSource();
            CommandSender sender = source.getSender();
            
            try {
                CommandResult result = command.run(ctx);
                CommandResult.showResult(sender, result);
                return Command.SINGLE_SUCCESS;
            } catch (Exception e) {
                sender.sendMessage(Component.empty()
                        .append(Component.text("An internal error occurred. See console for details."))
                        .color(NamedTextColor.RED)
                );
                Main.logger().log(Level.SEVERE, "An error occurred executing this command.", e);
                return 0;
            }
        };
    }
    
    public static PermissionedLiteralArgumentBuilder<CommandSourceStack> literal(@NotNull String literal, @NotNull PluginManager pluginManager) {
        return PermissionedLiteralArgumentBuilder.literal(literal, pluginManager);
    }
}
