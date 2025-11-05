package org.braekpo1nt.mctmanager.commands.mct.event.vote;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.commands.manager.CommandManager;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
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
                if (!VoteManager.votableGames().contains(gameToAdd)) {
                    return CommandResult.failure(Component.text("")
                            .append(Component.text(gameId)
                                    .decorate(TextDecoration.BOLD))
                            .append(Component.text(" is not a votable game")));
                }
                
                return gameManager.addGameToVotingPool(gameToAdd);
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    List<GameType> votingPool = gameManager.getVotingPool();
                    return VoteManager.votableGames().stream()
                            .filter(gameType -> !votingPool.contains(gameType))
                            .map(GameType::getId).toList();
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
                
                return gameManager.removeGameFromVotingPool(gameToRemove);
            }
            
            @Override
            public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length == 1) {
                    return gameManager.getVotingPool().stream().map(GameType::getId).toList();
                }
                return null;
            }
        });
    }
}
