package org.braekpo1nt.mctmanager.commands.commandmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.UsageCommandResult;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A bukkit command responsible for managing a set of sub commands.
 * Add classes which implement {@link SubCommand} to the {@link CommandManager#subCommands} map to add executable sub commands. 
 * Implement {@link TabSubCommand} in your sub command to provide tab completion
 */
public abstract class CommandManager extends TabSubCommand implements CommandExecutor {
    
    /**
     * Your super command's sub commands. You use your command manager to call one of these commands.
     * The key is the command's name (what you type in the chat to reference this command).format
     */
    protected final Map<String, SubCommand> subCommands = new HashMap<>();
    
    public CommandManager(@NotNull String name) {
        super(name);
    }
    
    /**
     * Add a new {@link SubCommand} implementation to the {@link CommandManager#subCommands} map.
     * This can now be called as a sub command using {@link SubCommand#getName()}.
     * @param subCommand the implementation of {@link SubCommand} to add
     */
    public void addSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName(), subCommand);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandResult commandResult = onSubCommand(sender, command, label, args);
        Component message = commandResult.getMessage();
        if (message != null) {
            sender.sendMessage(message);
        }
        return true;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return getUsage().with("<options>");
        }
        String subCommandName = args[0];
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null) {
            return CommandResult.failed(Component.text("Argument ")
                    .append(Component.text(subCommandName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not recognized.")));
        }
        CommandResult commandResult = subCommand.onSubCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        if (commandResult instanceof UsageCommandResult result) {
            return getUsage().with(result);
        }
        return commandResult;
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
