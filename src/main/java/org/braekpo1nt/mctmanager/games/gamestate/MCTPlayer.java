package org.braekpo1nt.mctmanager.games.gamestate;

import java.util.UUID;

public class MCTPlayer {
    private UUID uniqueId;
    private int score;
    private String teamName;
    
    
    public MCTPlayer(UUID uniqueId, int score, String teamName) {
        this.uniqueId = uniqueId;
        this.score = score;
        this.teamName = teamName;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public int getScore() {
        return score;
    }
    
    public String getTeamName() {
        return teamName;
    }
}
