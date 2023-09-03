package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.gson.JsonObject;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.NamespacedKey;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * 
 * @param world
 * @param startingLocations a set of starting locations that the players will be sent to a random one of
 * @param spectatorArea
 * @param layers
 * @param scores
 * @param durations
 * @param description
 */
record SpleefConfig(String world, List<Vector> startingLocations, BoundingBoxDTO spectatorArea, List<Layer> layers, Scores scores, Durations durations, JsonObject description) {
    
    BoundingBox getSpectatorArea() {
        return spectatorArea.getBoundingBox();
    }
    
    /**
     * @param structure the NamespacedKey of the structure to place for this layer
     * @param structureOrigin the origin to place the structure at
     * @param decayArea the area in which to decay blocks for this layer
     * @param decayRate the blocks/second to decay at
     */
    record Layer(NamespacedKey structure, Vector structureOrigin, BoundingBoxDTO decayArea, int decayRate) {
        BoundingBox getDecayArea() {
            return decayArea.getBoundingBox();
        }
    }
    
    record Scores(int survive) {
    }
    
    record Durations(int roundStarting, int decayTopLayers, int decayBottomLayers, int roundEnding) {
    }
}
