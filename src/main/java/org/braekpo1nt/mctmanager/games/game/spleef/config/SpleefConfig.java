package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public record SpleefConfig(String world, Vector startingLocation, BoundingBoxDTO spectatorArea, Scores scores, Durations durations, JsonObject description) {
    
    public BoundingBox getSpectatorArea() {
        return spectatorArea.getBoundingBox();
    }
    
    public record Scores(int survive) {
    }
    
    public record Durations(int roundStarting, int decayTopLayers, int decayBottomLayers, int roundEnding) {
    }
}
