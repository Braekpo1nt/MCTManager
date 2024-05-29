package org.braekpo1nt.mctmanager.games.colossalcombat.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ColossalCombatConfigController extends ConfigController<ColossalCombatConfigDTO> {
    
    private final File configFile;
    
    public ColossalCombatConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "colossalCombatConfig.json");
    }
    
    /**
     * Gets the config from storage 
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config
     */
    public @NotNull ColossalCombatConfig getConfig() throws ConfigException {
        ColossalCombatConfigDTO configDTO = loadConfigDTO(configFile, ColossalCombatConfigDTO.class);
        configDTO.validate(new Validator("colossalCombatConfig"));
        return configDTO.toConfig();
    }
    
}
