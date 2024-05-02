package org.braekpo1nt.mctmanager.commands.commandmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class SubCommandManager implements TabSubCommand {
    
    protected final Map<String, SubCommand> subCommands = new HashMap<>();
    
    public abstract Component getUsageMessage();
    
    @Override
    public @NotNull CommandResult onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
        return subCommand.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
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
