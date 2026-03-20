package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.braekpo1nt.mctmanager.games.gamestate.MCTPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class MaintenanceState extends StorageUtilState {
    
    public MaintenanceState(@NotNull GameStateStorageUtil context) {
        super(context);
    }
    
    @Override
    public void enter() {
        
    }
    
    @Override
    public void exit() {
        
    }
    
    public CompletableFuture<?> addNewPlayer(@NotNull UUID playerToJoin, @NotNull String name, @NotNull String teamId) throws ConfigIOException, SQLException {
        MCTPlayerEntity player = gameState.addPlayer(playerToJoin, name, teamId);
        return CompletableFuture.supplyAsync(() -> {
            try {
                gameStateService.addParticipant(fromPlayer(player));
            } catch (SQLException e) {
                throw new CompletionException("error joining player to team", e);
            }
            return null;
        });
    }
    
}
