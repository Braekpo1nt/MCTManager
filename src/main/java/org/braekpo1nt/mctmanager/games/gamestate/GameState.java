package org.braekpo1nt.mctmanager.games.gamestate;


import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<MCTPlayer> players;
    private List<MCTTeam> teams;
    
    public GameState(List<MCTPlayer> players, List<MCTTeam> teams) {
        this.players = players;
        this.teams = teams;
    }
    
    public GameState() {
        players = new ArrayList<>();
        teams = new ArrayList<>();
    }
    
    public List<MCTPlayer> getPlayers() {
        return players;
    }
    
    public List<MCTTeam> getTeams() {
        return teams;
    }
}
