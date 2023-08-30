package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public record SpleefConfig(String world, Vector startingLocation, BoundingBoxDTO spectatorBoundary, Scores scores, Durations durations) {
    
    public BoundingBox getSpectatorBoundary() {
        return spectatorBoundary.getBoundingBox();
    }
    
    public record Scores(int survive) {
    }
    
    public record Durations(int decayTopLayers, int decayBottomLayers) {
    }
}
