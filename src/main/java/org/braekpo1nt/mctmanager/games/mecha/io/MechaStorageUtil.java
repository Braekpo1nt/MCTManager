package org.braekpo1nt.mctmanager.games.mecha.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;

import java.io.*;

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
    
}
