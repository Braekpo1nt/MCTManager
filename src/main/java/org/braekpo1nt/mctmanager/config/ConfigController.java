package org.braekpo1nt.mctmanager.config;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Loads configs from files and saves configs to files
 * @param <T> the type of ConfigDTO this is responsible for
 */
public abstract class ConfigController<T> {
    
    /**
     * The Gson returned from this method will be used for both
     * serialization and deserialization of the config.
     * @return a Gson instance
     */
    protected @NotNull Gson getGson() {
        return ConfigUtils.GSON;
    }
    
    /**
     * Load the configDTO from the given file
     * @param configFile the file to load the config from
     * @param configType the type of config to load from the file
     * @return The configDTO stored in the file
     * @throws ConfigInvalidException if there is a problem parsing the JSON into a configDTO
     * @throws ConfigIOException if there is an IO problem getting the configDTO
     */
    public @NotNull T loadConfigDTO(@NotNull File configFile, @NotNull Class<T> configType) throws ConfigInvalidException, ConfigIOException {
        try {
            if (!configFile.exists()) {
                throw new ConfigIOException(String.format("Could not find config file %s", configFile));
            }
        } catch (SecurityException e) {
            throw new ConfigIOException(String.format("Security exception while trying to read %s", configFile), e);
        }
        try {
            Reader reader = new FileReader(configFile);
            T configDTO = getGson().fromJson(reader, configType);
            reader.close();
            return configDTO;
        } catch (IOException | JsonIOException e) {
            throw new ConfigIOException(String.format("Error while reading %s", configFile), e);
        } catch (JsonParseException e) {
            throw new ConfigInvalidException(String.format("Error parsing %s", configFile), e);
        }
    }
    
    /**
     * Saves the given Config to storage
     * @param configDTO the config to save
     * @param configFile the file to save the config to
     * @throws ConfigIOException if there is an IO error saving the config to the file system
     */
    public void saveConfigDTO(@NotNull T configDTO, @NotNull File configFile) throws ConfigIOException {
        try {
            // Check if the directory of the file exists, if not create it
            File configDirectory = configFile.getParentFile();
            if (!configDirectory.exists()) {
                boolean dirCreated = configDirectory.mkdirs();
                if (!dirCreated) {
                    throw new IOException("Failed to create directory: " + configDirectory.getAbsolutePath());
                }
            }
            // Check if the file exists, if not create it
            if (!configFile.exists()) {
                boolean fileCreated = configFile.createNewFile();
                if (!fileCreated) {
                    throw new IOException("Failed to create file: " + configFile.getAbsolutePath());
                }
            }
        } catch (SecurityException e) {
            throw new ConfigIOException(String.format("Security exception while trying to save config to %s", configFile), e);
        } catch (IOException e) {
            throw new ConfigIOException(String.format("Error while trying to create %s", configFile), e);
        }
        
        try {
            Writer writer = new FileWriter(configFile, false);
            getGson().toJson(configDTO, writer);
            writer.flush();
            writer.close();
        } catch (JsonIOException | IOException e) {
            throw new ConfigIOException(String.format("Error while writing %s", configFile), e);
        }
    }
    
}
