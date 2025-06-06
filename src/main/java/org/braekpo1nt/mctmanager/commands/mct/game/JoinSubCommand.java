package org.braekpo1nt.mctmanager.commands.mct.game;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.manager.TabSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JoinSubCommand extends TabSubCommand {
    
    private final @NotNull GameManager gameManager;
    
    public JoinSubCommand(@NotNull GameManager gameManager, @NotNull String name) {
        super(name);
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull CommandResult onSubCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 2) {
            return CommandResult.failure(getUsage().of("<gameID>").of("[configFile]"));
        }
        if (!(sender instanceof Player player)) {
            return CommandResult.failure("Only a player can use this command");
        }
        if (args.length == 0) {
            List<GameInstanceId> activeGameIds = gameManager.getActiveGameIds();
            if (activeGameIds.size() == 1) {
                GameInstanceId id = activeGameIds.getFirst();
                return joinToGame(player, id.getGameType(), id.getConfigFile());
            } else if (activeGameIds.isEmpty()) {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("No games are active right now")));
            } else {
                return CommandResult.failure(Component.empty()
                        .append(Component.text("Multiple games are active right now, please specify a game type")));
            }
        }
        
        String gameID = args[0];
        GameType gameType = GameType.fromID(gameID);
        if (gameType == null) {
            return CommandResult.failure(Component.text(gameID)
                    .append(Component.text(" is not a valid game")));
        }
        
        String configFile;
        if (args.length == 2) {
            configFile = args[1];
        } else {
            configFile = null;
        }
        
        return joinToGame(player, gameType, configFile);
    }
    
    public @NotNull CommandResult joinToGame(@NotNull Player player, @NotNull GameType gameType, @Nullable String configFile) {
        Participant participant = gameManager.getOnlineParticipant(player.getUniqueId());
        if (participant != null) {
            return gameManager.joinParticipantToGame(gameType, configFile, participant.getUniqueId());
        }
        
        if (gameManager.isAdmin(player.getUniqueId())) {
            return gameManager.joinAdminToGame(gameType, configFile, player);
        }
        return CommandResult.failure("Only a participant or an admin can use this command");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return gameManager.tabCompleteActiveGame(args);
    }
}
