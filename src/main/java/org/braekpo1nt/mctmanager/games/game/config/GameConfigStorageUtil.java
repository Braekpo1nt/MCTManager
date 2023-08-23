package org.braekpo1nt.mctmanager.games.game.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;

import java.io.*;

/**
 * A storage utility for saving and loading config files for games
 * @param <T> The 
 */
public abstract class GameConfigStorageUtil<T> {
    
    protected final File configDirectory;
    protected final String configFileName;
    /**
     * The class object representing the type of the configuration object. Used by gson for instantiating from json. 
     */
    protected Class<T> configClass;
    
    /**
     * @param plugin the plugin
     * @param configFileName The filename of the config file
     * @param configClass The class object representing the type of the configuration object. Used by gson for instantiating from json.
     */
    public GameConfigStorageUtil(Main plugin, String configFileName, Class<T> configClass) {
        this.configDirectory = plugin.getDataFolder().getAbsoluteFile();
        this.configFileName = configFileName;
        this.configClass = configClass;
    }
    
    public void loadConfig() throws IOException {
        Gson gson = new Gson();
        File configFile = getConfigFile();
        Reader reader = new FileReader(configFile);
        setConfig(gson.fromJson(reader, configClass));
        reader.close();
        if (getConfig() == null) {
            setConfig(initializeConfig());
        }
        Bukkit.getLogger().info(String.format("[MCTManager] Loaded %s", configFileName));
    }
    
    public void saveConfig() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File configFile = getConfigFile();
        Writer writer = new FileWriter(configFile, false);
        if (getConfig() == null) {
            setConfig(initializeConfig());
        }
        gson.toJson(getConfig(), writer);
        writer.flush();
        writer.close();
        Bukkit.getLogger().info(String.format("[MCTManager] Saved %s", configFileName));
    }
    
    protected abstract T getConfig();
    
    protected abstract void setConfig(T config);
    
    /**
     * Return a basic, empty instance of Config
     * @return an initial instance of type U 
     */
    protected abstract T initializeConfig();
    
    /**
     * Return the config file. If the file doesn't exist,
     * this creates the file.
     * @return The config file holding the saved instance of the config
     * @throws IOException If there is an error creating the file (which happens if the file doesn't exist)
     */
    protected File getConfigFile() throws IOException {
        File configFile = new File(configDirectory.getAbsolutePath(), configFileName);
        if (!configFile.exists()) {
            if (!configDirectory.exists()) {
                configDirectory.mkdirs();
            }
            configFile.createNewFile();
        }
        return configFile;
    }
    
}
