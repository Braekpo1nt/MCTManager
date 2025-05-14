package org.braekpo1nt.mctmanager.games.gamestate.preset;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Keeps the most recently loaded Preset in memory, so we don't need to perform file IO
 * for every call for information (e.g. tab completions)
 */
public class PresetStorageUtil {
    
    private final PresetController controller;
    private Preset preset = new Preset();
    
    public PresetStorageUtil(Main plugin) {
        this.controller = new PresetController(new File(plugin.getDataFolder(), "presets"));
    }
    
    /**
     * Save the preset to its file
     * @throws ConfigIOException
     */
    public void savePreset(@NotNull String presetFile) throws ConfigIOException {
        controller.savePreset(preset, presetFile);
    }
    
    /**
     * Load the preset from its file
     * @throws ConfigException
     */
    public void loadPreset(@NotNull String presetFile) throws ConfigException {
        this.preset = controller.getPreset(presetFile);
    }
    
    /**
     * @return the Preset currently stored in memory. This may not be up-to-date with the current file
     * in all situations, so to ensure up-to-date-ness, use {@link PresetStorageUtil#loadPreset(String)}
     */
    public Preset getPreset() {
        return preset;
    }
    
}
