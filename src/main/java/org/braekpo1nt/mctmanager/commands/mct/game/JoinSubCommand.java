package org.braekpo1nt.mctmanager.commands.mct.game;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.argumenttypes.ConfigFileArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.GameIdArgumentType;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameInstanceId;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JoinSubCommand implements BrigadierSubCommand {
    
    private final static String GAME_ID_ARG = "gameId";
    
    private final @NotNull GameManager gameManager;
    
    public JoinSubCommand(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("join")
                .executes(BrigadierAdapters.wraps(this::executeJoin))
                .then(Permissioned.argument(GAME_ID_ARG, new GameIdArgumentType(gameManager, true))
                        .executes(BrigadierAdapters.wraps(this::executeJoinGame))
                        .then(Permissioned.argument("configFile", new ConfigFileArgumentType(gameManager, true, GAME_ID_ARG))
                                .executes(BrigadierAdapters.wraps(this::executeJoinGameConfig))
                        )
                )
                ;
    }
    
    private @NotNull CommandResult executeJoin(@NotNull CommandContext<CommandSourceStack> ctx) {
        List<GameInstanceId> activeGameIds = gameManager.getActiveGameIds();
        if (activeGameIds.size() == 1) {
            GameInstanceId id = activeGameIds.getFirst();
            if (!(ctx.getSource().getSender() instanceof Player player)) {
                return CommandResult.failure("Only a player can use this command");
            }
            return joinToGame(player, id.getGameType(), id.getConfigFile());
        } else if (activeGameIds.isEmpty()) {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("No games are active right now")));
        } else {
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Multiple games are active right now, please specify a game type")));
        }
    }
    
    private @NotNull CommandResult executeJoinGame(@NotNull CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return CommandResult.failure("Only a player can use this command");
        }
        GameType gameType = ctx.getArgument(GAME_ID_ARG, GameType.class);
        return joinToGame(player, gameType, null);
    }
    
    private @NotNull CommandResult executeJoinGameConfig(@NotNull CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return CommandResult.failure("Only a player can use this command");
        }
        GameType gameType = ctx.getArgument(GAME_ID_ARG, GameType.class);
        String configFile = ctx.getArgument("configFile", String.class);
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
}
