package org.braekpo1nt.mctmanager.games.game.footrace.config;

import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;


record FootRaceConfig(String world, Vector startingLocation, BoundingBoxDTO finishLine, BoundingBoxDTO spectatorArea, Scores scores) {
    public BoundingBox getFinishLine() {
        return finishLine.getBoundingBox();
    }
    
    public BoundingBox getSpectatorArea() {
        return spectatorArea.getBoundingBox();
    }
    
    public record Scores(int completeLap, int[] placementPoints, int detriment) {
    }
    
    
}
