package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.gson.JsonElement;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayStage;
import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 
 * @param world the world the game is in
 * @param startingLocations a set of starting locations that the players will be sent to a random one of
 * @param spectatorArea the area the spectators shouldn't be able to leave
 * @param layers the layers of spleef
 * @param decayStages the stages of decay (must have at least 1). The last stage will go on forever, regardless of the duration or minParticipants values
 * @param tool the tool players receive to break the blocks (if null, a diamond shovel will be used).
 * @param rounds the number of rounds
 * @param scores the scores for spleef
 * @param durations the durations for spleef
 * @param description the description of spleef
 */
record SpleefConfig(String version, String world, List<Vector> startingLocations, BoundingBoxDTO spectatorArea, List<Layer> layers, List<DecayStage> decayStages, @Nullable ItemStackDTO tool, int rounds, Scores scores, Durations durations, JsonElement description) {
    
    /**
     * @param structure the NamespacedKey of the structure to place for this layer
     * @param structureOrigin the origin to place the structure at
     * @param decayArea the area in which to decay blocks for this layer
     */
    record Layer(NamespacedKey structure, Vector structureOrigin, BoundingBoxDTO decayArea) {
    }
    
    record Scores(int survive) {
    }
    
    record Durations(int roundStarting, int roundEnding) {
    }
}
