package org.braekpo1nt.mctmanager.commands.commandmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A bukkit command responsible for managing a set of sub commands.
 * Add classes which implement {@link SubCommand} to the {@link CommandManager#subCommands} map to add executable sub commands. 
 * Implement {@link TabSubCommand} in your sub command to provide tab completion
 */
public abstract class CommandManager implements CommandExecutor, TabSubCommand {
    
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
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null) {
            sender.sendMessage(Component.text("Argument ")
                    .append(Component.text(subCommandName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not recognized."))
                    .color(NamedTextColor.RED));
            return true;
        }
        
        CommandResult commandResult = subCommand.onSubCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        Component message = commandResult.getMessage();
        if (message != null) {
            sender.sendMessage(message);
        }
        return true;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return CommandResult.failed(getUsageMessage());
        }
        String subCommandName = args[0];
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null) {
            return CommandResult.failed(Component.text("Argument ")
                    .append(Component.text(subCommandName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not recognized.")));
        }
        return subCommand.onSubCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream().sorted().toList();
        }
        if (args.length > 1) {
            String subCommandName = args[0];
            if (!subCommands.containsKey(subCommandName)) {
                return Collections.emptyList();
            }
            SubCommand subCommand = subCommands.get(subCommandName);
            if (subCommand instanceof TabCompleter subTabCommand) {
                return subTabCommand.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }
}
