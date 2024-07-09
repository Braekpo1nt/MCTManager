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
    
    private final File configFile;
    
    public ParkourPathwayConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "parkourPathwayConfig.json");
    }
    
    @Override
    protected @NotNull Gson getGson() {
        return ConfigUtils.PRETTY_GSON;
    }
    
    /**
     * Gets the config from storage 
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config
     */
    public @NotNull ParkourPathwayConfig getConfig() throws ConfigException {
        ParkourPathwayConfigDTO configDTO = loadConfigDTO(configFile, ParkourPathwayConfigDTO.class);
        configDTO.validate(new Validator("parkourPathwayConfig"));
        return configDTO.toConfig();
    }
    
    public void validateConfig(@NotNull ParkourPathwayConfig config) throws ConfigInvalidException {
        ParkourPathwayConfigDTO configDTO = ParkourPathwayConfigDTO.fromConfig(config);
        configDTO.validate(new Validator("parkourPathwayConfig"));
    }
    
    /**
     * Save the given config to its file. Note that this does not validate the configDTO.
     * @param config the config to save
     * @throws ConfigIOException if there is an IO error while saving the config file
     */
    public void saveConfig(@NotNull ParkourPathwayConfig config) throws ConfigIOException {
        ParkourPathwayConfigDTO configDTO = ParkourPathwayConfigDTO.fromConfig(config);
        saveConfigDTO(configDTO, configFile);
    }
}
