package org.braekpo1nt.mctmanager.games.gamestate.preset;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PresetController extends ConfigController<PresetDTO> {
    
    private final @NotNull File presetDirectory;
    
    public PresetController(@NotNull File presetDirectory) {
        this.presetDirectory = presetDirectory;
    }
    
    @Override
    protected @NotNull Gson getGson() {
        return Main.GSON_PRETTY;
    }
    
    public @NotNull Preset getPreset(@NotNull String presetFile) throws ConfigException {
        PresetDTO presetDTO = loadConfigDTO(new File(presetDirectory, presetFile), PresetDTO.class);
        presetDTO.validate(new Validator("preset"));
        return presetDTO.toPreset();
    }
    
    public void savePreset(@NotNull Preset preset, @NotNull String presetFile) {
        PresetDTO presetDTO = PresetDTO.fromPreset(preset);
        saveConfigDTO(presetDTO, new File(presetDirectory, presetFile));
    }
    
    @Override
    public @NotNull PresetDTO loadConfigDTO(@NotNull File configFile, @NotNull Class<PresetDTO> configType) throws ConfigInvalidException, ConfigIOException {
        if (!configFile.exists()) {
            return new PresetDTO();
        }
        return super.loadConfigDTO(configFile, configType);
    }
}
