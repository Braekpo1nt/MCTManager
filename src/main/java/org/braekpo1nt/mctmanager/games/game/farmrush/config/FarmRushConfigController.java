package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FarmRushConfigController extends ConfigController<FarmRushConfigDTO> {
    
    private final File configFile;
    
    public FarmRushConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "farmRushConfig.json");
    }
    
    public @NotNull FarmRushConfig getConfig() throws ConfigException {
        FarmRushConfigDTO configDTO = loadConfigDTO(configFile, FarmRushConfigDTO.class);
        configDTO.validate(new Validator("farmRushConfig"));
        return configDTO.toConfig();
    }
    
}
