package org.braekpo1nt.mctmanager.games.game.footrace.config;

import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.Vector;


record FootRaceConfig(String world, Vector startingLocation, BoundingBoxDTO spectatorArea, Scores scores) {
    public record Scores(int completeLap, int[] placementPoints, int detriment) {
    }
    
    
}
