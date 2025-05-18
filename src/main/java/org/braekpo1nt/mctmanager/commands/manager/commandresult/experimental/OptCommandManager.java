package org.braekpo1nt.mctmanager.commands.manager.commandresult.experimental;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Allows for an argument to be placed before the subCommand
 * ["opt", "subCommandName", "arg1"]
 * So that subCommands get passed
 * ["opt", "arg1"]
 * Instead of what would normally be passed, which is just
 * ["arg1"]
 * This allows for commands like /preset config.json &lt;apply|add|join&gt; [options]
 * where the subCommand name happens after the config.json argument, making it a little
 * easier to run multiple commands at once.
 */
public abstract class OptCommandManager extends CommandManager {
    
    private final String optName;
    
    public OptCommandManager(@NotNull String name, @NotNull String optName) {
        super(name);
        this.optName = optName;
    }
    
    @Override
    public @NotNull Usage getUsage() {
        if (parent == null) {
            return new Usage(name).of(optName);
        }
        return parent.getUsage().of(name).of(optName);
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return noArgumentAction(sender, command, label);
        }
        if (args.length == 1) {
            return singleArgumentAction(sender, command, label, args[0]);
        }
        String subCommandName = args[1];
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand == null || !subCommand.hasPermission(sender)) {
            return CommandResult.failure(Component.text("Argument ")
                    .append(Component.text(subCommandName)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" is not recognized.")));
        }
        return subCommand.onSubCommand(sender, command, label, CommandUtils.removeElement(args, 1));
    }
    
    @Override
    protected @NotNull CommandResult noArgumentAction(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
        return CommandResult.failure(getUsage().of(getSubCommandUsageArg(sender)));
    }
    
    /**
     * Called when the only argument that was passed is the opt argument
     * (e.g. {@code /mct team preset preset.json} and no other args)
     * @param opt the opt argument
     * @return the result of the command
     */
    protected CommandResult singleArgumentAction(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String opt) {
        return CommandResult.failure(getUsage().of(getSubCommandUsageArg(sender)));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return onTabCompleteOpt(sender, command, label, args[0]);
        }
        if (args.length == 2) {
            return subCommands.keySet().stream().filter(subCommandName -> subCommands.get(subCommandName).hasPermission(sender)).sorted().toList();
        }
        if (args.length > 2) {
            String subCommandName = args[1];
            SubCommand subCommand = subCommands.get(subCommandName);
            if (subCommand == null || !subCommand.hasPermission(sender)) {
                return Collections.emptyList();
            }
            if (subCommand instanceof TabCompleter subTabCommand) {
                return subTabCommand.onTabComplete(sender, command, label, CommandUtils.removeElement(args, 1));
            }
        }
        return Collections.emptyList();
    }
    
    /**
     * The tab completion for the opt argument (e.g. the config file name)
     * @param opt what has been typed so far for opt
     * @return completion suggestions for the opt argument (before the sub command)
     */
    protected abstract @Nullable List<String> onTabCompleteOpt(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String opt);
    
}
