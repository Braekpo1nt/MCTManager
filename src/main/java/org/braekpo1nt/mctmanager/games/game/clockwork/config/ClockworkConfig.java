package org.braekpo1nt.mctmanager.games.game.clockwork.config;


import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record ClockworkConfig(String version, String world, Vector startingLocation, BoundingBoxDTO spectatorArea, List<WedgeDTO> wedges, int rounds, Scores scores, Durations durations, JsonObject description) {
    
    BoundingBox getSpectatorArea() {
        return spectatorArea.getBoundingBox();
    }
    
    record WedgeDTO(BoundingBoxDTO detectionArea) {
        
        BoundingBox getDetectionArea() {
            return detectionArea.getBoundingBox();
        }
        
    }
    
    record Scores(int playerElimination, int teamElimination, int winRound) {
    }
    
    record Durations() {
    }
}
