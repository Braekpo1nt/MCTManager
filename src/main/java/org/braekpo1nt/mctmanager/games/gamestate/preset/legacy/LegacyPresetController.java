package org.braekpo1nt.mctmanager.games.gamestate.preset.legacy;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigNotFoundException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class LegacyPresetController extends ConfigController<LegacyPresetDTO> {
    
    private final @NotNull File presetDirectory;
    
    public LegacyPresetController(@NotNull File presetDirectory) {
        this.presetDirectory = presetDirectory;
    }
    
    @Override
    protected @NotNull Gson getGson() {
        return Main.GSON_PRETTY;
    }
    
    /**
     * @param presetFile the file referencing the preset to load
     * @throws ConfigInvalidException if there is a problem parsing the JSON into a configDTO
     * @throws ConfigIOException if there is an IO problem getting the configDTO
     */
    public @NotNull LegacyPresetDTO getPreset(@NotNull File presetFile) throws ConfigException {
        LegacyPresetDTO presetDTO = loadConfigDTO(presetFile, LegacyPresetDTO.class);
        presetDTO.validate(new Validator("preset"));
        return presetDTO;
    }
    
    /**
     * @param presetFile the name of the preset file in the {@link #presetDirectory}
     * @throws ConfigInvalidException if there is a problem parsing the JSON into a configDTO
     * @throws ConfigIOException if there is an IO problem getting the configDTO
     */
    public @NotNull LegacyPresetDTO getPreset(@NotNull String presetFile) throws ConfigException {
        return getPreset(new File(presetDirectory, presetFile));
    }
    
    /**
     * @throws ConfigInvalidException if there is a problem parsing the JSON into a configDTO
     * @throws ConfigIOException if there is an IO problem getting the configDTO
     */
    @Override
    public @NotNull LegacyPresetDTO loadConfigDTO(@NotNull File configFile, @NotNull Class<LegacyPresetDTO> configType) throws ConfigInvalidException, ConfigIOException {
        if (!configFile.exists()) {
            throw new ConfigNotFoundException(configFile);
        }
        return super.loadConfigDTO(configFile, configType);
    }
}
