package org.braekpo1nt.mctmanager.games.gamemanager.event.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class EventConfigController extends ConfigController<EventConfigDTO> {
    
    private final File configFile;
    
    public EventConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "eventConfig.json");
    }
    
    /**
     * Gets the config from storage 
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config
     */
    public @NotNull EventConfig getConfig() throws ConfigException {
        EventConfigDTO configDTO = loadConfigDTO(configFile, EventConfigDTO.class);
        configDTO.validate(new Validator("eventConfig"));
        return configDTO.toConfig();
    }
    
}
