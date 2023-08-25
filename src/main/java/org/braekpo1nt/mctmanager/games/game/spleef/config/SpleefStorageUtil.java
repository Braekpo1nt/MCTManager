package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;

public class SpleefStorageUtil extends GameConfigStorageUtil<SpleefConfig> {
    
    protected SpleefConfig spleefConfig = null;
    
    public SpleefStorageUtil(Main plugin) {
        super(plugin, "spleefConfig.json", SpleefConfig.class);
    }
    
    @Override
    protected SpleefConfig getConfig() {
        return spleefConfig;
    }
    
    @Override
    protected void setConfig(SpleefConfig config) {
        this.spleefConfig = config;
    }
    
    @Override
    public SpleefConfig getDefaultConfig() {
        return new SpleefConfig();
    }
    
}
