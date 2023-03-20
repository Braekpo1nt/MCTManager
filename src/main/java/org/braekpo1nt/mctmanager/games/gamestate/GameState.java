package org.braekpo1nt.mctmanager.games.gamestate;

import java.util.ArrayList;
import java.util.List;

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
}
