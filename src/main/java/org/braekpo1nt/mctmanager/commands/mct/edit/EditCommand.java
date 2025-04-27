package org.braekpo1nt.mctmanager.commands.mct.edit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EditCommand extends CommandManager {
    
    public EditCommand(Main plugin, GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new StartSubCommand(plugin, gameManager, "start"));
        addSubCommand(new StopSubCommand(gameManager, "stop"));
        addSubCommand(new TabSubCommand("validate") {
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
            
            @Override
            public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    GameType gameType = gameManager.getEditorType();
                    if (gameType == null) {
                        return Collections.emptyList();
                    }
                    return CommandUtils.partialMatchTabList(
                            CommandUtils.getGameConfigs(gameType.getId()),
                            args[0]
                    );
                }
                return Collections.emptyList();
            }
        });
        addSubCommand(new TabSubCommand("save") {
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
                
                CommandResult result = gameManager.saveEditor(configFile, force);
                CommandUtils.refreshGameConfigs(plugin);
                return result;
            }
            
            @Override
            public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    GameType gameType = gameManager.getEditorType();
                    if (gameType == null) {
                        return Collections.emptyList();
                    }
                    return CommandUtils.partialMatchTabList(
                            CommandUtils.getGameConfigs(gameType.getId()),
                            args[0]
                    );
                }
                if (args.length == 2) {
                    return List.of("true", "false");
                }
                return Collections.emptyList();
            }
        });
        addSubCommand(new TabSubCommand("load") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 1) {
                    return CommandResult.failure(getUsage().of("<configFile>"));
                }
                if (!gameManager.editorIsRunning()) {
                    return CommandResult.failure(Component.text("No editor is running."));
                }
                
                String configFile = args[0];
                
                CommandUtils.refreshGameConfigs(plugin);
                return gameManager.loadEditor(configFile);
            }
            
            @Override
            public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    GameType gameType = gameManager.getEditorType();
                    if (gameType == null) {
                        return Collections.emptyList();
                    }
                    return CommandUtils.partialMatchTabList(
                            CommandUtils.getGameConfigs(gameType.getId()),
                            args[0]
                    );
                }
                return Collections.emptyList();
            }
        });
    }
}
