package org.braekpo1nt.mctmanager.games.gamestate;

import java.util.List;

public class MCTTeam {
    private String name;
    private int score;
    
    public MCTTeam(String name, int score) {
        this.name = name;
        this.score = score;
    }
    
    public String getName() {
        return name;
    }
    
    public int getScore() {
        return score;
    }
    
    @Override
    public String toString() {
        return "MCTTeam{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}
