package org.braekpo1nt.mctmanager.games.game.footrace.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class FootRaceStorageUtil extends GameConfigStorageUtil<FootRaceConfig> {
    public FootRaceStorageUtil(File configDirectory) {
        super(configDirectory, "footRaceConfig.json", FootRaceConfig.class);
    }
    
    @Override
    protected FootRaceConfig getConfig() {
        return null;
    }
    
    @Override
    protected boolean configIsValid(@Nullable FootRaceConfig config) throws IllegalArgumentException {
        return false;
    }
    
    @Override
    protected void setConfig(FootRaceConfig config) {
        
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return null;
    }
}
