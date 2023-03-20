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
    
    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public void setTeamName(String teamName) {
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
    
    @Override
    public String toString() {
        return "MCTPlayer{" +
                "uniqueId=" + uniqueId +
                ", score=" + score +
                ", teamName='" + teamName + '\'' +
                '}';
    }
}
