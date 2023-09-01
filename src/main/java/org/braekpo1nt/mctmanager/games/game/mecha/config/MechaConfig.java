package org.braekpo1nt.mctmanager.games.game.mecha.config;

import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.NamespacedKey;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * @param world the world
 * @param spawnLootTable The loot table for the spawn chests
 * @param removeArea The area to empty containers and remove floor items
 * @param weightedMechaLootTables The loot tables for the chests, with weights for the weighted random selection
 * @param initialBorderSize The size that the border should start at
 * @param borderStages The stages the border should progress through
 * @param spawnChestCoords The coordinates of the spawn chests
 * @param mapChestCoords the coordinates of the map chests
 * @param platformsStructure the structure to place the platforms
 * @param platformsRemovedStructure the structure to remove the platforms
 * @param platformsOrigin the origin to place the platformsStructure at
 */
record MechaConfig (String world, NamespacedKey spawnLootTable, BoundingBoxDTO removeArea, List<WeightedNamespacedKey> weightedMechaLootTables, double initialBorderSize, List<BorderStage> borderStages, List<Vector> spawnChestCoords, List<Vector> mapChestCoords, NamespacedKey platformsStructure, NamespacedKey platformsRemovedStructure, Vector platformsOrigin) {
    
    public BoundingBox getRemoveArea() {
        return removeArea.getBoundingBox();
    }
    
    record WeightedNamespacedKey(String namespace, String key, int weight) {
    }
    
    /**
     * 
     * @param size The size (in blocks) the border will be at this stage. The border will shrink from the previous stage's size to this stage's size over this stage's duration
     * @param delay the border will stay at the previous stage's size for this many seconds
     * @param duration the border will take this many seconds to transition from the previous stage's size to this stage's size
     */
    record BorderStage (int size, int delay, int duration){
    }
}
