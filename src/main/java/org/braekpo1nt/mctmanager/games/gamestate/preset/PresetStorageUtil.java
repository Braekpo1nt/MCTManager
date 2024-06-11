package org.braekpo1nt.mctmanager.games.gamestate.preset;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;

import java.util.logging.Logger;

/**
 * Keeps the most recently loaded Preset in memory, so we don't need to perform file IO
 * for every call for information (e.g. tab completions)
 */
public class PresetStorageUtil {
    
    private final Logger LOGGER;
    private final PresetController controller;
    private Preset preset = new Preset();
    
    public PresetStorageUtil(Main plugin) {
        this.LOGGER = plugin.getLogger();
        this.controller = new PresetController(plugin.getDataFolder());
    }
    
    /**
     * Save the preset to its file
     * @throws ConfigIOException
     */
    public void savePreset() throws ConfigIOException {
        controller.savePreset(preset);
    }
    
    /**
     * Load the preset from its file
     * @throws ConfigException
     */
    public void loadPreset() throws ConfigException {
        this.preset = controller.getPreset();
    }
    
    /**
     * @return the Preset currently stored in memory. This may not be up-to-date with the current file
     * in all situations, so to ensure up-to-date-ness, use {@link PresetStorageUtil#loadPreset()}
     */
    public Preset getPreset() {
        return preset;
    }
    
}
