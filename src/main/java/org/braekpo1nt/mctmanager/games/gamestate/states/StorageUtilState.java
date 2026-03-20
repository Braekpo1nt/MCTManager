package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamestate.GameState;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public abstract class StorageUtilState {
    
    protected final @NotNull GameStateStorageUtil context;
    protected final @NotNull GameState gameState;
    protected final @NotNull GameStateService gameStateService;
    
    public StorageUtilState(@NotNull GameStateStorageUtil context) {
        this.context = context;
        this.gameStateService = context.getGameStateService();
        this.gameState = context.getGameState();
    }
    
    public abstract void enter();
    
    public abstract void exit();
    
    public abstract void addTeam(String teamId, String teamDisplayName, String color) throws SQLException;
    
    public abstract void removeTeam(String teamId) throws SQLException;
    
    public abstract void addNewPlayer(@NotNull UUID playerToJoin, @NotNull String name, @NotNull String teamId) throws SQLException;
    
    public abstract void leavePlayer(UUID playerUniqueId) throws SQLException;
    
    public abstract void addAdmin(UUID adminUniqueId) throws SQLException;
    
    public abstract void removeAdmin(UUID adminUniqueId) throws SQLException;
}
