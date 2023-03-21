package org.braekpo1nt.mctmanager.games.gamestate;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the state of the game for saving and loading from disk. 
 */
public class GameState {
    private List<MCTPlayer> players;
    private List<MCTTeam> teams;
    
    public GameState() {
        this.players = new ArrayList<>();
        this.teams = new ArrayList<>();
    }
    
    public List<MCTPlayer> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<MCTPlayer> players) {
        this.players = players;
    }
    
    public List<MCTTeam> getTeams() {
        return teams;
    }
    
    public void setTeams(List<MCTTeam> teams) {
        this.teams = teams;
    }
    
    @Override
    public String toString() {
        return "GameState{" +
                "players=" + players +
                ", teams=" + teams +
                '}';
    }
    
    /**
     * Checks if the team with the given team name exists in the game state.
     * @param teamName The name of the team to search for
     * @return True if the team exists in the game state, false if not
     */
    public boolean containsTeam(String teamName) {
        for (MCTTeam team : teams) {
            if (team.getName().equals(teamName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Add a new team to the game state
     * @param teamName The internal name of the team
     * @param teamDisplayName The display name of the team
     * @param color The color of the team
     */
    public void addTeam(String teamName, String teamDisplayName, String color) {
        MCTTeam newTeam = new MCTTeam(teamName, teamDisplayName, 0, color);
        teams.add(newTeam);
    }
    
    
    /**
     * Removes the team with the given name from the game states, if it exists
     * @param teamName The internal name of the team to remove
     * @return True if the team was successfully deleted, 
     * false if the team did not exist in the first place.
     */
    public boolean removeTeam(String teamName) {
        for (MCTTeam team : teams) {
            if (team.getName().equals(teamName)) {
                teams.remove(team);
                return true;
            }
        }
        return false;
    }
    
    public void addPlayer(UUID playerUniqueId, String teamName) {
        MCTPlayer newPlayer = new MCTPlayer(playerUniqueId, 0, teamName);
        players.add(newPlayer);
    }
    
    /**
     * Checks if the game state contains the given player
     * @param playerUniqueId The UUID of the player to check for
     * @return True if the player with the given UUID exists, false otherwise 
     */
    public boolean containsPlayer(UUID playerUniqueId) {
        for (MCTPlayer mctPlayer : players) {
            if (mctPlayer.getUniqueId().equals(playerUniqueId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the player with the given UUID
     * @param playerUniqueId The UUID of the player to get
     * @return The player with the given UUID, null if the player does not exist.
     */
    public MCTPlayer getPlayer(UUID playerUniqueId) {
        for (MCTPlayer mctPlayer : players) {
            if (mctPlayer.getUniqueId().equals(playerUniqueId)) {
                return mctPlayer;
            }
        }
        return null;
    }
    
    public void removePlayer(UUID playerUniqueId) {
        for (MCTPlayer mctPlayer : players) {
            if (mctPlayer.getUniqueId().equals(playerUniqueId)) {
                players.remove(mctPlayer);
                return;
            }
        }
        
    }
}
