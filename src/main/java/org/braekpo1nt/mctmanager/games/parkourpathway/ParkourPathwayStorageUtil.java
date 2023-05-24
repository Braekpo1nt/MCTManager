package org.braekpo1nt.mctmanager.games.parkourpathway;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class ParkourPathwayStorageUtil {
    
    private final File configDirectory;
    protected ParkourPathwayConfig parkourPathwayConfig;
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
        Bukkit.getLogger().info("[MCTManager] Loaded " + CONFIG_FILE_NAME);
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
    
}
