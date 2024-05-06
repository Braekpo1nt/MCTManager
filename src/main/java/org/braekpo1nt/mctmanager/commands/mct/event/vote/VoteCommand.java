package org.braekpo1nt.mctmanager.commands.mct.event.vote;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.commandmanager.CommandManager;
import org.braekpo1nt.mctmanager.commands.commandmanager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.commandmanager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoteCommand extends CommandManager {
    
    public VoteCommand(GameManager gameManager, @NotNull String name) {
        super(name);
        addSubCommand(new TabSubCommand("add") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 1) {
                    return CommandResult.failure(getUsage().of("<game>"));
                }
                
                String gameString = args[0];
                GameType gameToAdd = GameType.fromID(gameString);
                if (gameToAdd == null) {
                    return CommandResult.failure(Component.text("")
                            .append(Component.text(gameString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized game")));
                }
                
                gameManager.getEventManager().addGameToVotingPool(sender, gameToAdd);
                return CommandResult.success();
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    return GameType.GAME_IDS.keySet().stream().sorted().toList();
                }
                return null;
            }
        });
        addSubCommand(new TabSubCommand("remove") {
            @Override
            public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 1) {
                    return CommandResult.failure(getUsage().of("<game>"));
                }
    
                String gameString = args[0];
                GameType gameToRemove = GameType.fromID(gameString);
                if (gameToRemove == null) {
                    return CommandResult.failure(Component.text("")
                            .append(Component.text(gameString)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized game")));
                }
                
                gameManager.getEventManager().removeGameFromVotingPool(sender, gameToRemove);
                return CommandResult.success();
            }
    
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    return GameType.GAME_IDS.keySet().stream().sorted().toList();
                }
                return null;
            }
        });
    }
}
