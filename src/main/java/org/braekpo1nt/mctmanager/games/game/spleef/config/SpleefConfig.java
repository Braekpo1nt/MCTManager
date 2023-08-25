package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class SpleefConfig {
    public String world;
    public Vector startingLocation;
    public BoundingBox spectatorBoundary;
    public SpleefScores scores;
    
    public SpleefConfig(String world, Vector startingLocation, BoundingBox spectatorBoundary, SpleefScores scores) {
        this.world = world;
        this.startingLocation = startingLocation;
        this.spectatorBoundary = spectatorBoundary;
        this.scores = scores;
    }
    
    static class SpleefScores {
        public int survive;
        SpleefScores(int survive) {
            this.survive = survive;
        }
    }
}
