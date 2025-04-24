package org.braekpo1nt.mctmanager.commands.mct.edit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.SubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class EditCommand extends CommandManager {
    
    public EditCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new StartSubCommand(gameManager, "start"));
        addSubCommand(new StopSubCommand(gameManager, "stop"));
        addSubCommand(new SubCommand("validate") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 1) {
                    return CommandResult.failure(getUsage().of("<configFile>"));
                }
                if (!gameManager.editorIsRunning()) {
                    return CommandResult.failure(Component.text("No editor is running."));
                }
                String configFile = args[0];
                return gameManager.validateEditor(configFile);
            }
        });
        addSubCommand(new SubCommand("save") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length < 1 || args.length > 2) {
                    return CommandResult.failure(getUsage().of("<configFile>").of("[true/false]"));
                }
                if (!gameManager.editorIsRunning()) {
                    return CommandResult.failure(Component.text("No editor is running."));
                }
                
                String configFile = args[0];
                
                boolean force = false;
                if (args.length == 2) {
                    String forceString = args[1];
                    Boolean forceBoolean = CommandUtils.toBoolean(forceString);
                    if (forceBoolean == null) {
                        return CommandResult.failure(Component.empty()
                                .append(Component.text(forceString)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" is not a valid boolean")));
                    }
                    force = forceBoolean;
                }
                
                return gameManager.saveEditor(configFile, force);
            }
        });
        addSubCommand(new SubCommand("load") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 1) {
                    return CommandResult.failure(getUsage().of("<configFile>"));
                }
                if (!gameManager.editorIsRunning()) {
                    return CommandResult.failure(Component.text("No editor is running."));
                }
                
                String configFile = args[0];
                
                return gameManager.loadEditor(configFile);
            }
        });
    }
}
