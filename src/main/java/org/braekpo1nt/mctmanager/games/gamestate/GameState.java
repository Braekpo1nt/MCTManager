package org.braekpo1nt.mctmanager.games.gamestate;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

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
    public boolean hasTeam(String teamName) {
        for (MCTTeam team : teams) {
            if (team.getName().equals(teamName)) {
                return true;
            }
        }
        return false;
    }
    
    public void addTeam(String teamName, String teamDisplayName, NamedTextColor color) {
        MCTTeam newTeam = new MCTTeam(teamName, teamDisplayName, 0, color);
        teams.add(newTeam);
    }
}
