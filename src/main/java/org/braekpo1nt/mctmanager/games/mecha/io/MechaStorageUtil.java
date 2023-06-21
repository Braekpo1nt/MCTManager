package org.braekpo1nt.mctmanager.games.mecha.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.*;

public class MechaStorageUtil {
    
    private final File configDirectory;
    protected MechaConfig mechaConfig;
    private static final String CONFIG_FILE_NAME = "mechaConfig.json";
    
    public MechaStorageUtil(Main plugin) {
        this.configDirectory = plugin.getDataFolder().getAbsoluteFile();
    }
    
    public void loadConfig() throws IOException {
        Gson gson = new Gson();
        File configFile = getConfigFile();
        Reader reader = new FileReader(configFile);
        mechaConfig = gson.fromJson(reader, MechaConfig.class);
        reader.close();
        if (mechaConfig == null) {
            mechaConfig = new MechaConfig();
        }
        Bukkit.getLogger().info("[MCTManager] Loaded MECHA config");
    }
    
    public void saveConfig() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File configFile = getConfigFile();
        Writer writer = new FileWriter(configFile, false);
        gson.toJson(this.mechaConfig, writer);
        writer.flush();
        writer.close();
        Bukkit.getLogger().info("[MCTManager] saved MECHA config");
    }
    
    private File getConfigFile() throws IOException {
        File configFile = new File(configDirectory.getAbsolutePath(), CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            if (!configDirectory.exists()) {
                configDirectory.mkdirs();
            }
            configFile.createNewFile();
        }
        return configFile;
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
