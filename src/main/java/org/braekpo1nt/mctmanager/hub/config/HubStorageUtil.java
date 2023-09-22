package org.braekpo1nt.mctmanager.hub.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class HubStorageUtil extends GameConfigStorageUtil<HubConfig> {
    /**
     * @param configDirectory The directory that the config should be located in (e.g. the plugin's data folder)
     */
    public HubStorageUtil(File configDirectory) {
        super(configDirectory, "hubConfig.json", HubConfig.class);
    }
    
    @Override
    protected HubConfig getConfig() {
        return null;
    }
    
    @Override
    protected boolean configIsValid(@Nullable HubConfig config) throws IllegalArgumentException {
        return false;
    }
    
    @Override
    protected void setConfig(HubConfig config) throws IllegalArgumentException {
        
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return null;
    }
}
