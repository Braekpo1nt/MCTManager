package org.braekpo1nt.mctmanager.games.game.footrace.config;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FootRaceConfigController extends ConfigController<FootRaceConfigDTO> {
    
    /**
     * The directory where all the config files are located for this game
     */
    private final File configDirectory;
    
    @Override
    protected @NotNull Gson getGson() {
        return ConfigUtils.PRETTY_GSON;
    }
    
    public FootRaceConfigController(@NotNull File pluginDataFolder, @NotNull String configDirectory) {
        this.configDirectory = new File(pluginDataFolder, configDirectory);
    }
    
    /**
     * Gets the config from storage 
     * @param configFile the name of the config file to use
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config, or the config file doesn't exist
     */
    public @NotNull FootRaceConfig getConfig(@NotNull String configFile) throws ConfigException {
        FootRaceConfigDTO configDTO = loadConfigDTO(new File(configDirectory, configFile), FootRaceConfigDTO.class);
        configDTO.validate(new Validator(configFile));
        return configDTO.toConfig();
    }
    
    // add the arg
    // TODO: this configFile argument is only used as a name, consider removing
    public void validateConfig(@NotNull FootRaceConfig config, @NotNull String configFile) {
        FootRaceConfigDTO configDTO = FootRaceConfigDTO.fromConfig(config);
        configDTO.validate(new Validator(configFile)); // set validate path
    }
    
    // add the arg
    public void saveConfig(@NotNull FootRaceConfig config, @NotNull String configFile) {
        FootRaceConfigDTO configDTO = FootRaceConfigDTO.fromConfig(config);
        saveConfigDTO(configDTO, new File(configDirectory, configFile)); // replace the config file
    }
}
