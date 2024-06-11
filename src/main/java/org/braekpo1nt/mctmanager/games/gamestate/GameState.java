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
    /**
     * holds the list of players who are to be added upon joining
     */
    @Builder.Default
    private @NotNull Map<String, OfflineMCTPlayer> offlinePlayers = new HashMap<>();
    @Builder.Default
    private @NotNull Map<String, MCTTeam> teams = new HashMap<>();
    @Builder.Default
    private @NotNull List<UUID> admins = new ArrayList<>();
    
    /**
     * Checks if the team with the given team name exists in the game state.
     * @param teamName The name of the team to search for
     * @return True if the team exists in the game state, false if not
     */
    public boolean containsTeam(String teamName) {
        return teams.containsKey(teamName);
    }
    
    /**
     * Add a new team to the game state
     * @param teamName The internal name of the team
     * @param teamDisplayName The display name of the team
     * @param color The color of the team
     */
    public void addTeam(String teamName, String teamDisplayName, String color) {
        MCTTeam newTeam = new MCTTeam(teamName, teamDisplayName, 0, color);
        teams.put(teamName, newTeam);
    }
    
    
    /**
     * Removes the team with the given name from the game states, if it exists.
     * If the team does not exist, nothing happens.
     * @param teamName The internal name of the team to remove
     */
    public void removeTeam(String teamName) {
        teams.remove(teamName);
    }
    
    /**
     * Adds the given player to the game state, joined to the given team
     * @param playerUniqueId the UUID of the player
     * @param teamName the teamId to join it to
     */
    public void addPlayer(UUID playerUniqueId, String teamName) {
        MCTPlayer newPlayer = new MCTPlayer(playerUniqueId, 0, teamName);
        players.put(playerUniqueId, newPlayer);
    }
    
    /**
     * Adds the given offline player to the game state, joined to the given team
     * @param ign the participant's in-game-name
     * @param offlineUniqueId can be null, but represents the offlineUniqueId of the participant
     * @param teamName the teamId of the team this participant belongs to
     */
    public void addOfflinePlayer(@NotNull String ign, @Nullable UUID offlineUniqueId, @NotNull String teamName) {
        OfflineMCTPlayer newPlayer = new OfflineMCTPlayer(offlineUniqueId, ign, teamName);
        offlinePlayers.put(ign, newPlayer);
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
     * @param ign the in-game-name of a participant who has never logged in before
     * @return true if the ign is in the current list of offline players (who have yet to log in for the first time), false otherwise
     */
    public boolean containsOfflineIGN(String ign) {
        return offlinePlayers.containsKey(ign);
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
     * @param ign the in-game-name of a participant who has never logged in before
     * @return the matching {@link OfflineMCTPlayer} of the ign
     */
    public OfflineMCTPlayer getOfflinePlayer(String ign) {
        return offlinePlayers.get(ign);
    }
    
    /**
     * Removes the player with the given UUID from the game state, if it exists.
     * If the player did not exist, nothing happens. 
     * @param playerUniqueId The UUID for the player
     */
    public void removePlayer(UUID playerUniqueId) {
        players.remove(playerUniqueId);
    }
    
    /**
     * Removes the offline player with the given IGN from the game state, if it exists.
     * If it did not exist, nothing happens. 
     * @param ign the in-game-name of a player who never logged in
     */
    public void removeOfflinePlayer(@NotNull String ign) {
        offlinePlayers.remove(ign);
    }
    
    public MCTTeam getTeam(String teamName) {
        return teams.get(teamName);
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
