package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CaptureTheFlagConfigController extends ConfigController<CaptureTheFlagConfigDTO> {
    
    private final File configFile;
    
    public CaptureTheFlagConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "captureTheFlagConfig.json");
    }
    
    public @NotNull CaptureTheFlagConfig getConfig() throws ConfigException {
        CaptureTheFlagConfigDTO configDTO = loadConfigDTO(configFile, CaptureTheFlagConfigDTO.class);
        configDTO.validate(new Validator("captureTheFlagConfig"));
        return configDTO.toConfig();
    }
    
}
