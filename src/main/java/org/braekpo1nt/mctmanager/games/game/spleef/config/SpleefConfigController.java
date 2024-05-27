package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class SpleefConfigController extends ConfigController<SpleefConfigDTO> {
    
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
        SpleefConfigDTO spleefConfigDTO = loadConfigDTO(configFile, SpleefConfigDTO.class);
        spleefConfigDTO.validate(new Validator("spleefConfig"));
        return spleefConfigDTO.toConfig();
    }
    
}
