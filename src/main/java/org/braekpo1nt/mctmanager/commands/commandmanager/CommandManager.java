package org.braekpo1nt.mctmanager.commands.commandmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A command that has other sub commands.
 * Add CommandExecutor implementing methods to the {@link CommandManager#subCommands} map to add executable sub commands. 
 * Implement TabExecutor in your sub command to provide tab completion
 */
public abstract class CommandManager implements TabExecutor {
    
    /**
     * Your super command's sub commands. You use your command manager to call one of these commands.
     * The key is the command's name (what you type in the chat to reference this command).format
     */
    protected final Map<String, SubCommand> subCommands = new HashMap<>();
    
    public abstract Component getUsageMessage();
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(getUsageMessage());
            return true;
        }
        String subCommandName = args[0];
        if (!subCommands.containsKey(subCommandName)) {
            sender.sendMessage(Component.text("Argument ")
                    .append(Component.text(subCommandName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not recognized."))
                    .color(NamedTextColor.RED));
            return true;
        }
        
        CommandResult commandResult = subCommands.get(subCommandName).onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        sender.sendMessage(commandResult.getMessage());
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream().sorted().toList();
        }
        if (args.length > 1) {
            String subCommandName = args[0];
            if (!subCommands.containsKey(subCommandName)) {
                return null;
            }
            SubCommand subCommand = subCommands.get(subCommandName);
            if (subCommand instanceof TabExecutor subTabCommand) {
                return subTabCommand.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return null;
    }
}
