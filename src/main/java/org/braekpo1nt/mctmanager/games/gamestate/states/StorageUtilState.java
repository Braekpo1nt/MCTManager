package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public abstract class StorageUtilState {
    
    protected final @NotNull GameStateStorageUtil context;
    
    public StorageUtilState(@NotNull GameStateStorageUtil context) {
        this.context = context;
    }
    
    public abstract void enter();
    
    public abstract void exit();
    
    /**
     * @param teamId the teamId
     * @param teamDisplayName the display name of the team
     * @param color the color string of the team
     * @return the score of the team (based on historical values)
     * @throws SQLException if there is a database error
     */
    public abstract int addTeam(String teamId, String teamDisplayName, String color) throws SQLException;
    
    public abstract void removeTeam(String teamId) throws SQLException;
    
    /**
     * @param uuid the UUID of the player to join
     * @param ign the ign of the player to join
     * @param teamId the teamId to join them to
     * @return the score of the participant (based on historical values)
     * @throws SQLException if there's a database error
     */
    public abstract int addNewPlayer(@NotNull UUID uuid, @NotNull String ign, @NotNull String teamId) throws SQLException;
    
    public abstract void leavePlayer(UUID playerUniqueId) throws SQLException;
    
    public abstract void addAdmin(UUID adminUniqueId) throws SQLException;
    
    public abstract void removeAdmin(UUID adminUniqueId) throws SQLException;
}
