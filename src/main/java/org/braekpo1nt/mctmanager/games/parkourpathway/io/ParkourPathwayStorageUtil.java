package org.braekpo1nt.mctmanager.games.parkourpathway.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.parkourpathway.CheckPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ParkourPathwayStorageUtil {
    
    private final File configDirectory;
    protected ParkourPathwayConfig parkourPathwayConfig = new ParkourPathwayConfig();
    private static final String CONFIG_FILE_NAME = "parkourPathwayConfig.json";
    
    public ParkourPathwayStorageUtil(Main plugin) {
        this.configDirectory = plugin.getDataFolder().getAbsoluteFile();
    }
    
    /**
     * Load the config file from storage
     * @throws IOException if there is a problem creating a new, reading the existing, 
     * or parsing the json of the config file
     */
    public void loadConfig() throws IOException {
        Gson gson = new Gson();
        File configFile = getConfigFile();
        Reader reader = new FileReader(configFile);
        parkourPathwayConfig = gson.fromJson(reader, ParkourPathwayConfig.class);
        reader.close();
        if (parkourPathwayConfig == null) {
            parkourPathwayConfig = new ParkourPathwayConfig();
        }
        Bukkit.getLogger().info("[MCTManager] Loaded parkour pathway config");
    }
    
    public void saveConfig() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File configFile = getConfigFile();
        Writer writer = new FileWriter(configFile, false);
        if (parkourPathwayConfig == null) {
            parkourPathwayConfig = new ParkourPathwayConfig();
        }
        gson.toJson(this.parkourPathwayConfig, writer);
        writer.flush();
        writer.close();
        Bukkit.getLogger().info("[MCTManager] saved parkour pathway config");
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
    
    public List<CheckPoint> getCheckPoints() {
        World checkpointWorld = Bukkit.getWorld(parkourPathwayConfig.getWorld());
        List<CheckPoint> newCheckpoints = new ArrayList<>();
        for (CheckPointConfig checkpointConfig : parkourPathwayConfig.getCheckpoints()) {
            Vector min = checkpointConfig.min();
            Vector max = checkpointConfig.max();
            BoundingBox boundingBox = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
            Vector configRespawn = checkpointConfig.respawn();
            Location respawn = new Location(checkpointWorld, configRespawn.getX(), configRespawn.getY(), configRespawn.getZ());
            newCheckpoints.add(new CheckPoint(checkpointConfig.yValue(), boundingBox, respawn));
        }
        return newCheckpoints;
    }
    
    public void setCheckpoints(List<CheckPoint> checkpoints) throws IOException {
        parkourPathwayConfig = new ParkourPathwayConfig();
        List<CheckPointConfig> checkpointConfigs = new ArrayList<>();
        for (CheckPoint checkpoint : checkpoints) {
            checkpointConfigs.add(new CheckPointConfig(checkpoint.yValue(), checkpoint.boundingBox().getMin(), checkpoint.boundingBox().getMax(), checkpoint.respawn().toVector()));
        }
        parkourPathwayConfig.setCheckpoints(checkpointConfigs);
        saveConfig();
    }
    
    public void setWorld(String world) throws IOException {
        parkourPathwayConfig.setWorld(world);
        saveConfig();
    }
}
