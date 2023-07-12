package org.braekpo1nt.mctmanager.games.mecha.io;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.io.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.util.Vector;

import java.util.*;

public class MechaStorageUtil extends GameConfigStorageUtil<MechaConfig> {
    
    protected MechaConfig mechaConfig = new MechaConfig();
    
    public MechaStorageUtil(Main plugin) {
        super(plugin, "mechaConfig.json", MechaConfig.class);
    }
    
    @Override
    protected MechaConfig getConfig() {
        return mechaConfig;
    }
    
    @Override
    protected void setConfig(MechaConfig config) {
        this.mechaConfig = config;
    }
    
    @Override
    protected MechaConfig initializeConfig() {
        return new MechaConfig();
    }
    
    public List<Vector> getSpawnChestCoords() {
        return mechaConfig.getSpawnChestCoords();
    }
    
    public void setSpawnChestCoords(List<Vector> spawnChestCoords) {
        mechaConfig.setSpawnChestCoords(spawnChestCoords);
    }
    
    public List<Vector> getMapChestCoords() {
        return mechaConfig.getMapChestCoords();
    }
    
    public void setMapChestCoords(List<Vector> mapChestCoords) {
        mechaConfig.setMapChestCoords(mapChestCoords);
    }
    
    public LootTable getSpawnLootTable() {
        NamespacedKey spawnLootTableNamespacedKey = mechaConfig.getSpawnLootTable();
        if (spawnLootTableNamespacedKey == null) {
            return LootTables.EMPTY.getLootTable();
        }
        return Bukkit.getLootTable(spawnLootTableNamespacedKey);
    }
    
    public void setSpawnLootTable(NamespacedKey lootTableNamespacedKey) {
        mechaConfig.setSpawnLootTable(lootTableNamespacedKey);
    }
    
    public Map<LootTable, Integer> getWeightedMechaLootTables() {
        List<WeightedNamespacedKey> weightedNamespacedKeys = mechaConfig.getWeightedMechaLootTables();
        Map<LootTable, Integer> weightedMechaLootTables = new HashMap<>(weightedNamespacedKeys.size());
        for (WeightedNamespacedKey weightedNamespacedKey : weightedNamespacedKeys) {
            String namespace = weightedNamespacedKey.namespace();
            String key = weightedNamespacedKey.key();
            int weight = weightedNamespacedKey.weight();
            LootTable lootTable = Bukkit.getLootTable(new NamespacedKey(namespace, key));
            weightedMechaLootTables.put(lootTable, weight);
        }
        return weightedMechaLootTables;
    }
    
    public void setWeightedMechaLootTables(Map<LootTable, Integer> weightedMechaLootTables) {
        List<WeightedNamespacedKey> weightedNamespacedKeys = new ArrayList<>(weightedMechaLootTables.size());
        for (Map.Entry<LootTable, Integer> entry : weightedMechaLootTables.entrySet()) {
            LootTable lootTable = entry.getKey();
            NamespacedKey namespacedKey = lootTable.getKey();
            String namespace = namespacedKey.getNamespace();
            String key = namespacedKey.getKey();
            int weight = entry.getValue();
            weightedNamespacedKeys.add(new WeightedNamespacedKey(namespace, key, weight));
        }
        mechaConfig.setWeightedMechaLootTables(weightedNamespacedKeys);
    }
}
