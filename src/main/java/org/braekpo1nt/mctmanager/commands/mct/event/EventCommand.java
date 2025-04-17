package org.braekpo1nt.mctmanager.commands.mct.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.CommandUtils;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.Usage;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.mct.event.vote.VoteCommand;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EventCommand extends CommandManager {
    
    public EventCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new TabSubCommand("start") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length < 1 || 2 < args.length) {
                    return CommandResult.failure(getUsage().of("<numberOfGames>").of("[currentGameNumber]"));
                }
                String maxGamesString = args[0];
                if (!CommandUtils.isInteger(maxGamesString)) {
                    return CommandResult.failure(Component.empty()
                            .append(Component.text(maxGamesString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not an integer")));
                }
                int maxGames = Integer.parseInt(maxGamesString);
                
                int currentGameNumber;
                if (args.length == 2) {
                    String currentGameNumberString = args[1];
                    if (!CommandUtils.isInteger(currentGameNumberString)) {
                        return CommandResult.failure(Component.empty()
                                .append(Component.text(currentGameNumberString)
                                        .decorate(TextDecoration.BOLD))
                                .append(Component.text(" is not an integer")));
                    }
                    currentGameNumber = Integer.parseInt(currentGameNumberString);
                    if (currentGameNumber < 1) {
                        return CommandResult.failure(Component.text("Current game number must be at least 1"));
                    }
                } else {
                    currentGameNumber = 1;
                }
                
                gameManager.getEventManager().startEvent(sender, maxGames, currentGameNumber);
                return CommandResult.success();
            }
            
            @Override
            public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
            public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                return Collections.emptyList();
            }
        });
        addSubCommand(new EventUndoSubCommand(gameManager, "undo"));
        addSubCommand(new VoteCommand(gameManager, "vote"));
        addSubCommand(new ModifyCommand(gameManager, "modify"));
    }
    
    @Override
    protected @NotNull Usage getSubCommandUsageArg(Permissible permissible) {
        return new Usage("<options>");
    }
}
