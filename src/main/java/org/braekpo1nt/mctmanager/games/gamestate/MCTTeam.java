package org.braekpo1nt.mctmanager.games.gamestate;

import lombok.Data;

@Data
class MCTTeam {
    private String name;
    private String displayName;
    private int score;
    private String color;
    
    public MCTTeam(String name, String displayName, int score, String color) {
        this.name = name;
        this.displayName = displayName;
        this.score = score;
        this.color = color;
    }
    
    @Override
    public String toString() {
        return "MCTTeam{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", score=" + score +
                ", color='" + color + '\'' +
                '}';
    }
}
