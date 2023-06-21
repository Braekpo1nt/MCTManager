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
import java.util.ArrayList;
import java.util.List;

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
    
    public List<LootTable> getMechaLootTables() {
        List<WeightedNamespacedKey> weightedMechaLootTables = mechaConfig.getWeightedMechaLootTables();
        List<LootTable> mechaLootTables = new ArrayList<>(weightedMechaLootTables.size());
        for (WeightedNamespacedKey weightedNamespacedKey : weightedMechaLootTables) {
            String namespace = weightedNamespacedKey.namespace();
            String key = weightedNamespacedKey.key();
            LootTable lootTable = Bukkit.getLootTable(new NamespacedKey(namespace, key));
            mechaLootTables.add(lootTable);
        }
        return mechaLootTables;
    }
    
    public List<Integer> getMechaLootTableWeights() {
        List<WeightedNamespacedKey> weightedMechaLootTables = mechaConfig.getWeightedMechaLootTables();
        List<Integer> weights = new ArrayList<>(weightedMechaLootTables.size());
        for (WeightedNamespacedKey weightedNamespacedKey : weightedMechaLootTables) {
            int weight = weightedNamespacedKey.weight();
            weights.add(weight);
        }
        return weights;
    }
    
    public void setWeightedMechaLootTables(List<NamespacedKey> namespacedKeys, List<Integer> weights) {
        List<WeightedNamespacedKey> weightedMechaLootTables = new ArrayList<>(namespacedKeys.size());
        for (int i = 0; i < namespacedKeys.size(); i++) {
            NamespacedKey namespacedKey = namespacedKeys.get(i);
            String key = namespacedKey.getKey();
            String namespace = namespacedKey.getNamespace();
            int weight = weights.get(i);
            WeightedNamespacedKey weightedNamespacedKey = new WeightedNamespacedKey(namespace, key, weight);
            weightedMechaLootTables.add(weightedNamespacedKey);
        }
        mechaConfig.setWeightedMechaLootTables(weightedMechaLootTables);
    }
    
}
