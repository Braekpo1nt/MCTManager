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
    
    private final File configFile;
    
    @Override
    protected @NotNull Gson getGson() {
        return ConfigUtils.PRETTY_GSON;
    }
    
    public FootRaceConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "footRaceConfig.json");
    }
    
    /**
     * Gets the config from storage 
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config
     */
    public @NotNull FootRaceConfig getConfig() throws ConfigException {
        FootRaceConfigDTO configDTO = loadConfigDTO(configFile, FootRaceConfigDTO.class);
        configDTO.validate(new Validator("footRaceConfig"));
        return configDTO.toConfig();
    }
    
    public void validateConfig(FootRaceConfig config) {
        FootRaceConfigDTO configDTO = FootRaceConfigDTO.fromConfig(config);
        configDTO.validate(new Validator("footRaceConfig"));
    }
    
    public void saveConfig(FootRaceConfig config) {
        FootRaceConfigDTO configDTO = FootRaceConfigDTO.fromConfig(config);
        saveConfigDTO(configDTO, configFile);
    }
}
