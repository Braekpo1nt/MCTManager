package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class SpleefConfig {
    public String world;
    public Vector startingLocation;
    public BoundingBox spectatorBoundary;
    public Scores scores;
    public Durations durations;
    
    public SpleefConfig(String world, Vector startingLocation, BoundingBox spectatorBoundary, Scores scores, Durations durations) {
        this.world = world;
        this.startingLocation = startingLocation;
        this.spectatorBoundary = spectatorBoundary;
        this.scores = scores;
        this.durations = durations;
    }
    
    public static class Scores {
        public int survive;
        Scores(int survive) {
            this.survive = survive;
        }
    }
    
    public static class Durations {
        public int decayTopLayers;
        public int decayBottomLayers;
    }
}
