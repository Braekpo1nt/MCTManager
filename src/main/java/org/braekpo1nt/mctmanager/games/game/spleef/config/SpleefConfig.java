package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.braekpo1nt.mctmanager.games.game.config.NamespacedKeyDTO;
import org.braekpo1nt.mctmanager.games.game.config.inventory.ItemStackDTO;
import org.braekpo1nt.mctmanager.games.game.spleef.DecayStage;
import org.braekpo1nt.mctmanager.games.game.spleef.powerup.Powerup;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @param world the world the game is in
 * @param startingLocations a set of starting locations that the players will be sent to a random one of
 * @param spectatorArea the area the spectators shouldn't be able to leave
 * @param stencilBlock the stencil block which the structures should have inside them to be replaced with the layerBlock upon start. If this is null, no replacement will be made (this is useful if your layer structures are already made of the stencil).
 * @param layerBlock the block that the stencil will be replaced with upon start which the floors are to be made of. The area(s) of replacement will be the BoundingBox formed by the layers decayAreas. If this is null, dirt will be used.
 * @param decayBlock the block type that blocks decay to before disappearing. If this is null, coarse dirt will be used.
 * @param layers the layers of spleef
 * @param decayStages the stages of decay (must have at least 1). The last stage will go on forever, regardless of the duration or minParticipants values
 * @param tool the tool players receive to break the blocks (if null, a diamond shovel will be used).
 * @param rounds the number of rounds
 * @param scores the scores for spleef
 * @param durations the durations for spleef
 * @param description the description of spleef
 */
record SpleefConfig(String version, String world, List<Vector> startingLocations, BoundingBoxDTO spectatorArea, @Nullable Material stencilBlock, @Nullable Material layerBlock, @Nullable Material decayBlock, List<Layer> layers, List<DecayStage> decayStages, @Nullable ItemStackDTO tool, int rounds, Powerups powerups, Scores scores, Durations durations, JsonElement description) {
    
    /**
     * @param structure the NamespacedKey of the structure to place for this layer
     * @param structureOrigin the origin to place the structure at
     * @param decayArea the area in which to decay blocks for this layer. If this is null, the size of the structure and structureOrigin will be used as the area.
     */
    record Layer(@Nullable NamespacedKeyDTO structure, Vector structureOrigin, @Nullable BoundingBoxDTO decayArea) {
    }
    
    /**
     * 
     * @param chancePerSecond every second, the player has this percentage chance to get a powerup. 0 means no powerups will be given at all each second. Defaults to 0.0
     * @param blockBreakChance every time the player breaks a block, they have this percentage chance to get a powerup. 0 means no powerups will be given upon breaking a block (ever). Defaults to 0.0
     * @param minTimeBetween the minimum time (in milliseconds) between getting powerups. Players should not get two powerups one after another immediately. 0 means no restriction. Defaults to 0
     * @param maxPowerups limit the number of powerups a player can have. If they are at max, they won't collect any more until they use some of them. 0 means players can't hold any powerups at all. Negative values indicate unlimited powerup collection. Defaults to 0
     */
    record Powerups(double chancePerSecond, double blockBreakChance, long minTimeBetween, int maxPowerups, @Nullable Map<Powerup.Type, @Nullable PowerupDTO> powerups) {
        void isValid() {
            Preconditions.checkArgument(0 <= chancePerSecond && chancePerSecond <= 1.0, "chancePerSecond must be between 0 and 1, inclusive");
            Preconditions.checkArgument(0 <= blockBreakChance && blockBreakChance <= 1.0, "blockBreakChance must be between 0 and 1, inclusive");
            Preconditions.checkArgument(minTimeBetween >= 0, "minTimeBetween must be greater than or equal to 0");
            if (powerups != null) {
                for (PowerupDTO powerupDTO : powerups.values()) {
                    if (powerupDTO != null) {
                        powerupDTO.isValid();
                    }
                }
            }
        }
        
        /**
         * @return a map of each {@link Powerup.Type} to its weight. This comes from the {@link Powerups#powerups} weight value, but if any entries are missing from the config, the weight is set to 1.
         */
        @NotNull Map<Powerup.Type, @NotNull Integer> getWeights() {
            if (this.powerups == null) {
                return SpleefStorageUtil.getDefaultWeights();
            }
            Map<Powerup.Type, @NotNull Integer> result = new HashMap<>(Powerup.Type.values().length);
            for (int i = 0; i < Powerup.Type.values().length; i++) {
                Powerup.Type type = Powerup.Type.values()[i];
                PowerupDTO powerupDTO = this.powerups.get(type);
                if (powerupDTO != null) {
                    int weight = powerupDTO.getWeight();
                    result.put(type, weight);
                } else {
                    result.put(type, 1);
                }
            }
            return result;
        }
        
    }
    
    record Scores(int survive) {
    }
    
    record Durations(int roundStarting, int roundEnding) {
    }
}
