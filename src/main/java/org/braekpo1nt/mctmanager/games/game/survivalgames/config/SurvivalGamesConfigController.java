package org.braekpo1nt.mctmanager.games.game.survivalgames.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class SurvivalGamesConfigController extends ConfigController<SurvivalGamesConfigDTO> {
    
    private final File configFile;
    private final File legacyMechaConfigFile;
    
    public SurvivalGamesConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "survivalGamesConfig.json");
        this.legacyMechaConfigFile = new File(configDirectory, "mechaConfig.json");
    }
    
    /**
     * Gets the config from storage 
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config
     */
    public @NotNull SurvivalGamesConfig getConfig() throws ConfigException {
        File fileToParse = configFile;
        try {
            if (!configFile.exists()) {
                try {
                    if (!legacyMechaConfigFile.exists()) {
                        throw new ConfigIOException(String.format("Could not find config file %s", configFile));
                    } else {
                        Bukkit.getLogger().info(String.format("Using legacy config file %s", legacyMechaConfigFile));
                        fileToParse = legacyMechaConfigFile;
                    }
                } catch (SecurityException e) {
                    throw new ConfigIOException(String.format("Security exception while trying to read %s", legacyMechaConfigFile), e);
                }
            }
        } catch (SecurityException e) {
            throw new ConfigIOException(String.format("Security exception while trying to read %s", configFile), e);
        }
        SurvivalGamesConfigDTO configDTO = loadConfigDTO(fileToParse, SurvivalGamesConfigDTO.class);
        configDTO.validate(new Validator("survivalGamesConfig"));
        return configDTO.toConfig();
    }
    
}
