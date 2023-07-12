package org.braekpo1nt.mctmanager.games.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.braekpo1nt.mctmanager.Main;

import java.io.*;

/**
 * A storage utility for saving and loading config files for games
 * @param <T> The 
 */
public abstract class GameConfigStorageUtil<T> {
    
    protected final File configDirectory;
    protected final String configFileName;
    protected T config;
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
        this.config = gson.fromJson(reader, configClass);
        reader.close();
        if (config == null) {
            config = initializeConfig();
        }
    }
    
    public void saveConfig() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File configFile = getConfigFile();
        Writer writer = new FileWriter(configFile, false);
        if (config == null) {
            config = initializeConfig();
        }
        gson.toJson(this.config, writer);
        writer.flush();
        writer.close();
    }
    
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
