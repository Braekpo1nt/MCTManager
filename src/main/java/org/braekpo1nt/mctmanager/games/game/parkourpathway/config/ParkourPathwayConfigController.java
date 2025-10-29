package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ParkourPathwayConfigController extends ConfigController<ParkourPathwayConfigDTO> {
    
    /**
     * The directory where all the config files are located for this game
     */
    private final File configDirectory;
    
    public ParkourPathwayConfigController(@NotNull File pluginDataFolder, @NotNull String configDirectory) {
        this.configDirectory = new File(pluginDataFolder, configDirectory);
    }
    
    @Override
    protected @NotNull Gson getGson() {
        return ConfigUtils.PRETTY_GSON;
    }
    
    /**
     * Gets the config from storage
     * @param configFile the name of the config file to use
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config, or the config file doesn't exist
     */
    public @NotNull ParkourPathwayConfig getConfig(@NotNull String configFile) throws ConfigException {
        ParkourPathwayConfigDTO configDTO = loadConfigDTO(new File(configDirectory, configFile), ParkourPathwayConfigDTO.class);
        configDTO.validate(new Validator(configFile));
        return configDTO.toConfig();
    }
    
    public void validateConfig(@NotNull ParkourPathwayConfig config, @NotNull String configFile) throws ConfigInvalidException {
        ParkourPathwayConfigDTO configDTO = ParkourPathwayConfigDTO.fromConfig(config);
        configDTO.validate(new Validator(configFile));
    }
    
    /**
     * Save the given config to its file. Note that this does not validate the configDTO.
     * @param config the config to save
     * @param configFile the file name to save to
     * @throws ConfigIOException if there is an IO error while saving the config file
     */
    public void saveConfig(@NotNull ParkourPathwayConfig config, @NotNull String configFile) throws ConfigIOException {
        ParkourPathwayConfigDTO configDTO = ParkourPathwayConfigDTO.fromConfig(config);
        saveConfigDTO(configDTO, new File(configDirectory, configFile));
    }
}
