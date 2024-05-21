package org.braekpo1nt.mctmanager.config;

import com.google.gson.*;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A storage utility for saving and loading config files
 * @param <T> The config type
 */
public abstract class ConfigStorageUtil<T> {
    
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
    public ConfigStorageUtil(File configDirectory, String configFileName, Class<T> configClass) {
        this.configDirectory = configDirectory;
        this.configFileName = configFileName;
        this.configFile = new File(this.configDirectory, configFileName);
        this.configClass = configClass;
    }
    
    /**
     * Loads the {@link ConfigStorageUtil#configFile} and stores it in memory. 
     * @throws IllegalArgumentException if the config file doesn't exist, if there are any exceptions thrown while reading/parsing the file, or if the loaded config is not valid.
     * @return true if the config loaded properly, false if there were any exceptions thrown while
     * loading the config file, if the file doesn't exist, or the config isn't valid.
     */
    public boolean loadConfig() throws IllegalArgumentException {
        T newConfig = getConfigFromFile();
        try {
            if (!configIsValid(newConfig)) {
                throw new IllegalArgumentException(String.format("configIsValid returned false for config file \"%s\"", configFile));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid config file: \"%s\"", configFile), e);
        }
        setConfig(newConfig);
        Bukkit.getLogger().info(String.format("[MCTManager] Loaded %s", configFileName));
        return true;
    }
    
    /**
     * Get the config from the config file and parse it into a {@link T} object 
     * @throws IllegalArgumentException if there were any IO, Json, or Security exceptions thrown from reading/parsing the config file, or if the config file doesn't exist.
     * @return a new {@link T} config object from the {@link ConfigStorageUtil#configFile}, null if the stored config in the file is null
     */
    protected @Nullable T getConfigFromFile() throws IllegalArgumentException {
        try {
            if (!configFile.exists()) {
                throw new IllegalArgumentException(String.format("%s not found.", configFile));
            }
        } catch (SecurityException e) {
            throw new IllegalArgumentException(String.format("Permission error while checking for existence of %s file", configFile), e);
        }
        try {
            Reader reader = new FileReader(configFile);
            T newConfig = ConfigUtils.GSON.fromJson(reader, configClass);
            reader.close();
            return newConfig;
        } catch (IOException | JsonIOException e) {
            throw new IllegalArgumentException(String.format("Error while reading %s", configFile), e);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException(String.format("Error parsing %s", configFile), e);
        }
    }
    
    /**
     * Saves the passed in config object to the {@link ConfigStorageUtil#configFile}.
     * If config us null, or there are any IO, security, or json errors, then 
     * an error is reported to the logger and an IOException is thrown
     * @param config the config to save
     * @throws IOException if there is an IO, security, or json error saving the file
     */
    protected void saveConfig(T config) throws IOException {
        try {
            if (!configFile.exists()) {
                if (!configDirectory.exists()) {
                    configDirectory.mkdirs();
                }
                configFile.createNewFile();
            }
        } catch (IOException e) {
            throw new IOException("Error creating config file:", e);
        } catch (SecurityException e) {
            throw new IOException("Permission error while checking for existence of config file:", e);
        }
    
        try {
            Writer writer = new FileWriter(configFile, false);
            Main.GSON_PRETTY.toJson(config, writer);
            writer.flush();
            writer.close();
            Bukkit.getLogger().info(String.format("[MCTManager] Saved default config to %s", configFile));
        } catch (JsonIOException | IOException e) {
            throw new IOException("Error writing to config file:", e);
        }
    }
    
    /**
     * Saves the config from {@link ConfigStorageUtil#getConfig()} to the 
     * {@link ConfigStorageUtil#configFile}. If there are any IO, security, or 
     * json errors, an error is reported to the logger and nothing happens.
     * @throws IOException if there is an IO, security, or json error saving the file
     */
    public void saveConfig() throws IOException {
        saveConfig(getConfig());
        Bukkit.getLogger().info(String.format("[MCTManager] Saved %s", configFileName));
    }
    
    /**
     * @return the saved config
     */
    protected abstract T getConfig();
    
    /**
     * Checks if the given config is valid. If the config is invalid, throws an IllegalArgumentException with a detailed message of what was invalid about the given config
     * @param config The config to validate
     * @throws IllegalArgumentException If the config is invalid. The exception includes a detailed message of what was invalid
     * @return true if the config is valid, false if not
     */
    protected abstract boolean configIsValid(@Nullable T config) throws IllegalArgumentException;
    
    /**
     * Sets this storage util's config to the given config, thus saving it to memory for later use.
     * This may involve assigning some variables (e.g. getting a bukkit world one time and setting it to a field
     * instead of using Bukkit.getWorld() every time this.getWorld() is called).
     * <p> 
     * This assumes that the config has been validated according to {@link ConfigStorageUtil#configIsValid(Object)}, and throws IllegalArgumentException if that assumption turns out to be false
     * @throws IllegalArgumentException if the config, which is assumed to be valid according to {@link ConfigStorageUtil#configIsValid(Object)}, turns out to be invalid. 
     * @param config the config to use
     */
    protected abstract void setConfig(T config) throws IllegalArgumentException;
    
    /**
     * @return An InputStream holding an example json-serialized {@link T} config
     */
    protected abstract InputStream getExampleResourceStream();
    
    /**
     * Returns a new instance of the example config. Note that the returned config instance may
     * be modified, so this returns a fresh instance to avoid errors with future uses. 
     * @return A new config instance with example values.
     * null if there is a problem reading or parsing the example config
     */
    protected @Nullable T getExampleConfig() {
        InputStream inputStream = getExampleResourceStream();
        if (inputStream == null) {
            return null;
        }
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return ConfigUtils.GSON.fromJson(reader, configClass);
    }
    
}
