package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FarmRushConfigController extends ConfigController<FarmRushConfigDTO> {
    
    /**
     * The directory where all the config files are located for this game
     */
    private final File configDirectory;
    
    public FarmRushConfigController(@NotNull File pluginDataFolder, @NotNull String configDirectory) {
        this.configDirectory = new File(pluginDataFolder, configDirectory);
    }
    
    /**
     * Gets the config from storage 
     * @param configFile the name of the config file to use
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config, or the config file doesn't exist
     */
    public @NotNull FarmRushConfig getConfig(@NotNull String configFile) throws ConfigException {
        FarmRushConfigDTO configDTO = loadConfigDTO(new File(configDirectory, configFile), FarmRushConfigDTO.class);
        configDTO.validate(new Validator(configFile));
        return configDTO.toConfig();
    }
    
}
