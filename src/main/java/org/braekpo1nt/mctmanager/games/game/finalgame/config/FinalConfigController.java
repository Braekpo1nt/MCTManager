package org.braekpo1nt.mctmanager.games.game.finalgame.config;

import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FinalConfigController extends ConfigController<FinalConfigDTO> {
    
    /**
     * The directory where all the config files are located for this game
     */
    private final File configDirectory;
    
    public FinalConfigController(@NotNull File pluginDataFolder, @NotNull String configDirectory) {
        this.configDirectory = new File(pluginDataFolder, configDirectory);
    }
    
    public @NotNull FinalConfig getConfig(@NotNull String configFile) throws ConfigException {
        FinalConfigDTO configDTO = loadConfigDTO(new File(configDirectory, configFile), FinalConfigDTO.class);
        configDTO.validate(new Validator(configFile));
        return configDTO.toConfig();
    }
}
