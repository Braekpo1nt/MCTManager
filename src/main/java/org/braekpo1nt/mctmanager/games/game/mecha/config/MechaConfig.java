package org.braekpo1nt.mctmanager.games.game.mecha.config;

import org.braekpo1nt.mctmanager.games.game.config.BoundingBoxDTO;
import org.bukkit.NamespacedKey;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

record MechaConfig (String world, NamespacedKey spawnLootTable, BoundingBoxDTO removeArea, List<WeightedNamespacedKey> weightedMechaLootTables, double initialBorderSize, List<BorderStage> borderStages, List<Vector> spawnChestCoords, List<Vector> mapChestCoords) {
    
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
