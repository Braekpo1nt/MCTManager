package org.braekpo1nt.mctmanager.games.game.farmrush.config;

import org.braekpo1nt.mctmanager.config.ConfigController;

import java.io.File;

public class FarmRushConfigController extends ConfigController<FarmRushConfigDTO> {
    
    private final File configFile;
    
    public FarmRushConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "farmRushConfig.json");
    }
    
}
