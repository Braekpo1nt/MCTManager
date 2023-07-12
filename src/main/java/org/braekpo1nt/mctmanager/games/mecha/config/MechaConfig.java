package org.braekpo1nt.mctmanager.games.mecha.config;

import org.bukkit.NamespacedKey;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MechaConfig {
    
    private List<Vector> spawnChestCoords;
    private List<Vector> mapChestCoords;
    private NamespacedKey spawnLootTable;
    private List<WeightedNamespacedKey> weightedMechaLootTables;
    
    public MechaConfig() {
        this.spawnChestCoords = new ArrayList<>();
        this.mapChestCoords = new ArrayList<>();
        this.spawnLootTable = null;
        this.weightedMechaLootTables = new ArrayList<>();
    }
    
    public List<Vector> getSpawnChestCoords() {
        return spawnChestCoords;
    }
    
    public void setSpawnChestCoords(List<Vector> spawnChestCoords) {
        this.spawnChestCoords = spawnChestCoords;
    }
    
    public List<Vector> getMapChestCoords() {
        return mapChestCoords;
    }
    
    public void setMapChestCoords(List<Vector> mapChestCoords) {
        this.mapChestCoords = mapChestCoords;
    }
    
    public NamespacedKey getSpawnLootTable() {
        return spawnLootTable;
    }
    
    public void setSpawnLootTable(NamespacedKey spawnLootTable) {
        this.spawnLootTable = spawnLootTable;
    }
    
    public List<WeightedNamespacedKey> getWeightedMechaLootTables() {
        return weightedMechaLootTables;
    }
    
    public void setWeightedMechaLootTables(List<WeightedNamespacedKey> weightedMechaLootTables) {
        this.weightedMechaLootTables = weightedMechaLootTables;
    }
}
