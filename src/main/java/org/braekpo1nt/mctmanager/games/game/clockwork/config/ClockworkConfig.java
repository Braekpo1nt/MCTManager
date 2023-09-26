package org.braekpo1nt.mctmanager.games.game.clockwork.config;


import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record ClockworkConfig(String version, String world, Vector startingLocation, BoundingBoxDTO spectatorArea, Chaos chaos, List<WedgeDTO> wedges, int rounds, Sound clockChime, double initialChimeInterval, double chimeIntervalDecrement, Scores scores, Durations durations, JsonObject description) {
    
    record WedgeDTO(BoundingBoxDTO detectionArea) {
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
    
    public record Chaos(Cylinder cylinder, MinMaxInc arrows, MinMaxInc fallingBlocks, MinMaxDec summonDelay, MinMaxFloat arrowSpeed, MinMaxFloat arrowSpread) {
        public record Cylinder(double centerX, double centerZ, double radius, MinMax spawnY) {
        }
        
        public record MinMax(double min, double max) {
        }
    
        public record MinMaxFloat(float min, float max) {
        }
        
        public record MinMaxInc(MinMax initial, MinMax increment) {
        }
        
        public record MinMaxDec(MinMax initial, MinMax decrement) {
        }
    }
}
