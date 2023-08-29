package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public record SpleefConfig(String world, Vector startingLocation, BoundingBox spectatorBoundary, Scores scores, Durations durations) {
    
    public record Scores(int survive) {
    }
    
    public record Durations(int decayTopLayers, int decayBottomLayers) {
    }
}
