package org.braekpo1nt.mctmanager.commands.mct.event.vote;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.voting.VoteManager;
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
                
                String gameId = args[0];
                GameType gameToAdd = GameType.fromID(gameId);
                if (gameToAdd == null) {
                    return CommandResult.failure(Component.text("")
                            .append(Component.text(gameId)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a recognized game")));
                }
                if (!VoteManager.votableGameIds().contains(gameId)) {
                    return CommandResult.failure(Component.text("")
                            .append(Component.text(gameId)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is votable game")));
                }
                
                gameManager.getEventManager().addGameToVotingPool(sender, gameToAdd);
                return CommandResult.success();
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    return VoteManager.votableGameIds();
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
                    return VoteManager.votableGameIds();
                }
                return null;
            }
        });
    }
}
