package org.braekpo1nt.mctmanager.games.game.config;

import com.google.gson.*;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A storage utility for saving and loading config files for games
 * @param <T> The 
 */
public abstract class GameConfigStorageUtil<T> {
    
    protected final File configDirectory;
    protected final String configFileName;
    protected final File configFile;
    /**
     * The class object representing the type of the configuration object. Used by gson for instantiating from json. 
     */
    protected Class<T> configClass;
    
    /**
     * @param configDirectory The directory that the config should be located in (e.g. the plugin's data folder)
     * @param configFileName The filename of the config file
     * @param configClass The class object representing the type of the configuration object. Used by gson for instantiating from json.
     */
    public GameConfigStorageUtil(File configDirectory, String configFileName, Class<T> configClass) {
        this.configDirectory = configDirectory;
        this.configFileName = configFileName;
        this.configFile = new File(this.configDirectory, configFileName);
        this.configClass = configClass;
    }
    
    /**
     * Loads the {@link GameConfigStorageUtil#configFile} and stores it in memory. 
     * if the config file doesn't exist, if there are any exceptions thrown while reading/parsing the file, or if the loaded config is not valid, prints the reason to the Bukkit log and returns null.
     * 
     * @return true if the config loaded properly, false if there were any exceptions thrown while
     * loading the config file, if the file doesn't exist, or the values aren't valid.
     */
    public boolean loadConfig() {
        T newConfig = getConfigFromFile();
        if (newConfig == null) {
            return false;
        }
        if (!configIsValid(newConfig)) {
            return false;
        }
        setConfig(newConfig);
        Bukkit.getLogger().info(String.format("[MCTManager] Loaded %s", configFileName));
        return true;
    }
    
    /**
     * Get the config from the config file. If the config file doesn't exist, 
     * or if there are any errors reading the config file, returns null.
     * @return a new {@link T} config object from the {@link GameConfigStorageUtil#configFile}, 
     * or null if there were any IO, Json, or Security exceptions thrown from reading/parsing
     * the config file. Null if there is no config file yet.
     */
    protected @Nullable T getConfigFromFile() {
        try {
            if (!configFile.exists()) {
                Bukkit.getLogger().severe(String.format("%s not found.", configFile));
                return null;
            }
        } catch (SecurityException e) {
            Bukkit.getLogger().severe(String.format("Permission error while checking for existence of %s file: \n%s", configFile, e));
            e.printStackTrace();
            return null;
        }
        T newConfig = null;
        try {
            Reader reader = new FileReader(configFile);
            Gson gson = new Gson();
            newConfig = gson.fromJson(reader, configClass);
            reader.close();
        } catch (IOException | JsonIOException e) {
            Bukkit.getLogger().severe(String.format("Error while reading %s: \n%s", configFile, e));
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            Bukkit.getLogger().severe(String.format("Error parsing %s: \n%s", configFile, e));
            e.printStackTrace();
        }
        return newConfig;
    }
    
    /**
     * Saves the passed in config object to the {@link GameConfigStorageUtil#configFile}.
     * If config us null, or there are any IO, security, or json errors, then 
     * an error is reported to the logger and nothing happens.
     * @param config the config to save
     */
    protected void saveConfig(T config) {
        try {
            if (!configFile.exists()) {
                if (!configDirectory.exists()) {
                    configDirectory.mkdirs();
                }
                configFile.createNewFile();
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe(String.format("Error creating config file: \n%s", e));
        } catch (SecurityException e) {
            Bukkit.getLogger().severe(String.format("Permission error while checking for existence of config file: \n%s", e));
        }
        
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Writer writer = new FileWriter(configFile, false);
            gson.toJson(config, writer);
            writer.flush();
            writer.close();
            Bukkit.getLogger().info(String.format("[MCTManager] Saved default config to %s", configFile));
        } catch (JsonIOException | IOException e) {
            Bukkit.getLogger().severe(String.format("Error writing to config file: \n%s", e));
        }
    }
    
    /**
     * Saves the config from {@link GameConfigStorageUtil#getConfig()} to the 
     * {@link GameConfigStorageUtil#configFile}. If there are any IO, security, or 
     * json errors, an error is reported to the logger and nothing happens.
     */
    public void saveConfig() {
        saveConfig(getConfig());
        Bukkit.getLogger().info(String.format("[MCTManager] Saved %s", configFileName));
    }
    
    protected abstract T getConfig();
    
    /**
     * Checks if the given config is valid. If the config is invalid, prints the reason to the console
     * and returns null
     * @param config The config to validate
     * @return true if the config is valid, false if not
     */
    protected abstract boolean configIsValid(T config);
    
    protected abstract void setConfig(T config);
    
    /**
     * @return An InputStream holding the default json file contents for
     * the {@link T} config
     */
    protected abstract InputStream getDefaultResourceStream();
    
    /**
     * Returns a new instance of the default config. Note that the returned config instance may
     * be modified, so this should return a fresh instance to avoid errors with future default
     * uses. 
     * @return A new config instance with default values for use if no user-config is present.
     * null if there is a problem reading or parsing the default config
     */
    protected @Nullable T getDefaultConfig() {
        InputStream inputStream = getDefaultResourceStream();
        if (inputStream == null) {
            return null;
        }
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        return gson.fromJson(reader, configClass);
    }
    
}
