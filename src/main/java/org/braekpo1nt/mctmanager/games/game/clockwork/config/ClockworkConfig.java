package org.braekpo1nt.mctmanager.games.game.clockwork.config;


import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record ClockworkConfig(String version, String world, Vector startingLocation, BoundingBoxDTO spectatorArea, List<WedgeDTO> wedges, int rounds, Sound clockChime, Scores scores, Durations durations, JsonObject description) {
    
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
    
    record Durations(int breather, int getToWedge, int stayOnWedge) {
    }
    
    /**
     * 
     * @param sound Is the name/id/title of the sound to play. Can be a built-in minecraft sound or a resource pack sound. See sound parameter explanation of default minecraft /playsound command
     * @param volume see volume parameter explanation of default minecraft /playsound command
     * @param pitch see pitch parameter explanation of default minecraft /playsound command
     */
    record Sound(String sound, float volume, float pitch) {
    }
}
