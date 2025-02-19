package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the state of the game for saving and loading from disk. 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameState {
    @Builder.Default
    private @NotNull Map<UUID, MCTPlayer> players = new HashMap<>();
    @Builder.Default
    private @NotNull Map<String, MCTTeam> teams = new HashMap<>();
    @Builder.Default
    private @NotNull List<UUID> admins = new ArrayList<>();
    
    /**
     * Checks if the team with the given team name exists in the game state.
     * @param teamId The name of the team to search for
     * @return True if the team exists in the game state, false if not
     */
    public boolean containsTeam(String teamId) {
        return teams.containsKey(teamId);
    }
    
    /**
     * Add a new team to the game state
     * @param teamId The internal name of the team
     * @param teamDisplayName The display name of the team
     * @param color The color of the team
     */
    public void addTeam(String teamId, String teamDisplayName, String color) {
        MCTTeam newTeam = new MCTTeam(teamId, teamDisplayName, 0, color);
        teams.put(teamId, newTeam);
    }
    
    
    /**
     * Removes the team with the given name from the game states, if it exists.
     * If the team does not exist, nothing happens.
     * @param teamId The internal name of the team to remove
     */
    public void removeTeam(String teamId) {
        teams.remove(teamId);
    }
    
    /**
     * Adds the given player to the game state, joined to the given team
     * @param playerUniqueId the UUID of the player
     * @param teamId the teamId to join it to
     */
    public void addPlayer(@NotNull UUID playerUniqueId, @NotNull String name, @NotNull String teamId) {
        MCTPlayer newPlayer = new MCTPlayer(playerUniqueId, name, 0, teamId);
        players.put(playerUniqueId, newPlayer);
    }
    
    /**
     * Checks if the game state contains the given player
     * @param playerUniqueId The UUID of the player to check for
     * @return True if the player with the given UUID exists, false otherwise 
     */
    public boolean containsPlayer(UUID playerUniqueId) {
        return players.containsKey(playerUniqueId);
    }
    
    /**
     * Returns the player with the given UUID
     * @param playerUniqueId The UUID of the player to get
     * @return The player with the given UUID, null if the player does not exist.
     */
    public @Nullable MCTPlayer getPlayer(@NotNull UUID playerUniqueId) {
        return players.get(playerUniqueId);
    }
    
    /**
     * Removes the player with the given UUID from the game state, if it exists.
     * If the player did not exist, nothing happens. 
     * @param playerUniqueId The UUID for the player
     */
    public void removePlayer(UUID playerUniqueId) {
        players.remove(playerUniqueId);
    }
    
    MCTTeam getTeam(String teamId) {
        return teams.get(teamId);
    }
    
    /**
     * Checks if the game state contains the given admin
     * @param adminUniqueId The unique id of the admin
     * @return True if the unique id is in the admin list, false otherwise
     */
    public boolean isAdmin(UUID adminUniqueId) {
        return admins.contains(adminUniqueId);
    }
    
    public void addAdmin(UUID adminUniqueId) {
        admins.add(adminUniqueId);
    }
    
    public void removeAdmin(UUID adminUniqueId) {
        admins.remove(adminUniqueId);
    }
}
