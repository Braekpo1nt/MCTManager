package org.braekpo1nt.mctmanager.games.game.spleef.config;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class SpleefConfigController {
    
    private final File configFile;
    
    public SpleefConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "spleefConfig.json");
    }
    
    /**
     * Gets the config from storage 
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config
     */
    public @NotNull SpleefConfig getConfig() throws ConfigInvalidException, ConfigIOException {
        SpleefConfigDTO spleefConfigDTO = loadConfigDTO();
        spleefConfigDTO.validate(new Validator("spleefConfig"));
        return spleefConfigDTO.toConfig();
    }
    
    /**
     * Load the configDTO from file storage
     * @return The configDTO stored in the file
     * @throws ConfigInvalidException if there is a problem parsing the JSON into a configDTO
     * @throws ConfigIOException if there is an IO problem getting the configDTO
     */
    private SpleefConfigDTO loadConfigDTO() throws ConfigInvalidException, ConfigIOException {
        try {
            if (!configFile.exists()) {
                throw new ConfigIOException(String.format("Could not find config file %s", configFile));
            }
        } catch (SecurityException e) {
            throw new ConfigIOException(String.format("Security exception while trying to read %s", configFile), e);
        }
        try {
            Reader reader = new FileReader(configFile);
            SpleefConfigDTO configDTO = ConfigUtils.GSON.fromJson(reader, SpleefConfigDTO.class);
            reader.close();
            return configDTO;
        } catch (IOException  | JsonIOException e) {
            throw new ConfigIOException(String.format("Error while reading %s", configFile), e);
        } catch (JsonSyntaxException e) {
            throw new ConfigInvalidException(String.format("Error parsing %s", configFile), e);
        }
    }
    
    /**
     * Saves the given Config to storage
     * @param config the config to save
     * @throws ConfigIOException if there is an IO error saving the config to the file system
     */
    private void saveConfigDTO(SpleefConfig config) {
        try {
            if (!configFile.exists()) {
                if (!configFile.mkdirs()) {
                    throw new ConfigIOException(String.format("Unable to create directories for %s", configFile));
                }
                if (configFile.createNewFile()) {
                    throw new ConfigIOException(String.format("Unable to create config file %s", configFile));
                }
            }
        } catch (SecurityException e) {
            throw new ConfigIOException(String.format("Security exception while trying to save config to %s", configFile), e);
        } catch (IOException e) {
            throw new ConfigIOException(String.format("Error while trying to create %s", configFile), e);
        }
        
        SpleefConfigDTO configDTO = SpleefConfigDTO.fromConfig(config);
        
        try {
            Writer writer = new FileWriter(configFile, false);
            ConfigUtils.GSON.toJson(configDTO, writer);
            writer.flush();
            writer.close();
        } catch (JsonIOException | IOException e) {
            throw new ConfigIOException(String.format("Error while writing %s", configFile), e);
        }
    }
    
}
