package org.braekpo1nt.mctmanager.games.game.mecha.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MechaConfigController extends ConfigController<MechaConfigDTO> {
    
    private final File configFile;
    
    public MechaConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "mechaConfig.json");
    }
    
    /**
     * Gets the config from storage 
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config
     */
    public @NotNull MechaConfig getConfig() throws ConfigException {
        MechaConfigDTO configDTO = loadConfigDTO(configFile, MechaConfigDTO.class);
        configDTO.validate(new Validator("mechaConfig"));
        return configDTO.toConfig();
    }
    
}
