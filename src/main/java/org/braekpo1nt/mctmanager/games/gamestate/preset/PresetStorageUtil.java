package org.braekpo1nt.mctmanager.games.gamestate.preset;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;

import java.util.logging.Logger;

public class PresetStorageUtil {
    
    private final Logger LOGGER;
    private final PresetController controller;
    private Preset preset = new Preset();
    
    public PresetStorageUtil(Main plugin) {
        this.LOGGER = plugin.getLogger();
        this.controller = new PresetController(plugin.getDataFolder());
    }
    
    public void savePreset() throws ConfigIOException {
        controller.savePreset(preset);
    }
    
    public void loadPreset() throws ConfigException {
        this.preset = controller.getPreset();
    }
    
    public Preset getPreset() {
        return preset;
    }
    
}
