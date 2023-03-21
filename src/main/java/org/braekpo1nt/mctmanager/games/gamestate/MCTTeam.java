package org.braekpo1nt.mctmanager.games.gamestate;

import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class MCTTeam {
    private String name;
    private String displayName;
    private int score;
    private NamedTextColor color;
    
    public MCTTeam(String name, String displayName, int score, NamedTextColor color) {
        this.name = name;
        this.displayName = displayName;
        this.score = score;
        this.color = color;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    @Override
    public String toString() {
        return "MCTTeam{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }

    public NamedTextColor getColor() {
        return color;
    }
}
