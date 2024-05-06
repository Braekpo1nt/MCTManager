package org.braekpo1nt.mctmanager.commands.mct.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.*;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventCommand extends CommandManager {
    
    public EventCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new TabSubCommand("start") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                int maxGames = 6;
                if (args.length != 1) {
                    return CommandResult.failure(getUsage().of("<numberOfGames>"));
                }
                String maxGamesString = args[0];
                if (!CommandUtils.isInteger(maxGamesString)) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(maxGamesString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not an integer")));
                }
                maxGames = Integer.parseInt(maxGamesString);
                gameManager.getEventManager().startEvent(sender, maxGames);
                return CommandResult.success();
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return Collections.emptyList();
            }
        });
        addSubCommand(new TabSubCommand("stop") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (!gameManager.getEventManager().eventIsActive()) {
                    return CommandResult.failure(Component.text("There is no event running."));
                }
                if (args.length != 1) {
                    return CommandResult.success(Component.text("Are you sure? Type ")
                            .append(Component.empty()
                                    .append(Component.text("/mct event stop "))
                                    .append(Component.text("confirm")
                                            .decorate(TextDecoration.BOLD))
                                    .decorate(TextDecoration.ITALIC))
                            .append(Component.text(" to confirm."))
                            .color(NamedTextColor.YELLOW));
                }
                String confirmString = args[0];
                if (!confirmString.equals("confirm")) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(confirmString))
                            .append(Component.text(" is not a recognized option.")));
                }
                gameManager.getEventManager().stopEvent(sender);
                return CommandResult.success();
            }
        
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return Collections.emptyList();
            }
        });
        addSubCommand(new SubCommand("pause") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.getEventManager().pauseEvent(sender);
                return CommandResult.success();
            }
        });
        addSubCommand(new SubCommand("resume") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                gameManager.getEventManager().resumeEvent(sender);
                return CommandResult.success();
            }
        });
        addSubCommand(new FinalGameSubCommand(gameManager, "finalgame"));
        addSubCommand(new EventUndoSubCommand(gameManager, "undo"));
//        subCommands.put("vote", new VoteSubCommand(gameManager));
//        subCommands.put("modify", new ModifySubCommand(gameManager));
    }
    
    @Override
    protected @NotNull Usage getUsageOptions() {
        return new Usage("<options>");
    }
}
