package org.braekpo1nt.mctmanager.games.gamestate;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

/**
 * Represents the state of the game for saving and loading from disk. 
 */
public class GameState {
    private Map<UUID, MCTPlayer> players;
    private Map<String, MCTTeam> teams;
    
    public GameState() {
        this.players = new HashMap<>();
        this.teams = new HashMap<>();
    }
    
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
    
    public void addPlayer(UUID playerUniqueId, String teamName) {
        MCTPlayer newPlayer = new MCTPlayer(playerUniqueId, 0, teamName);
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
    public MCTPlayer getPlayer(UUID playerUniqueId) {
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
    
    public MCTTeam getTeam(String teamName) {
        return teams.get(teamName);
    }
    
    public Map<UUID, MCTPlayer> getPlayers() {
        return players;
    }
    
    public void setPlayers(Map<UUID, MCTPlayer> players) {
        this.players = players;
    }
    
    public Map<String, MCTTeam> getTeams() {
        return teams;
    }
    
    public void setTeams(Map<String, MCTTeam> teams) {
        this.teams = teams;
    }
}
