package org.braekpo1nt.mctmanager.games.game.clockwork.config;


import com.google.gson.JsonElement;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.config.dto.net.kyori.adventure.sound.SoundDTO;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.List;

record ClockworkConfig(String version, String world, Vector startingLocation, BoundingBoxDTO spectatorArea, Chaos chaos, List<WedgeDTO> wedges, int rounds, SoundDTO clockChime, double initialChimeInterval, double chimeIntervalDecrement, Team.OptionStatus collisionRule, Scores scores, Durations durations, JsonElement description) {
    
    record WedgeDTO(BoundingBoxDTO detectionArea) {
    }
    
    record Scores(int playerElimination, int teamElimination, int winRound) {
    }
    
    record Durations(int breather, int getToWedge, int stayOnWedge) {
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
