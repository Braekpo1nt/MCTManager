package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.Data;

import java.util.UUID;

@Data
class MCTPlayer {
    private UUID uniqueId;
    private int score;
    private String teamName;
    
    public MCTPlayer(UUID uniqueId, int score, String teamName) {
        this.uniqueId = uniqueId;
        this.score = score;
        this.teamName = teamName;
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
