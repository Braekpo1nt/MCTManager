package org.braekpo1nt.mctmanager.games.game.spleef.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.mecha.config.MechaConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.InputStream;

public class SpleefStorageUtil extends GameConfigStorageUtil<SpleefConfig> {
    
    protected SpleefConfig spleefConfig = null;
    
    public SpleefStorageUtil(File configDirectory) {
        super(configDirectory, "spleefConfig.json", SpleefConfig.class);
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
    protected boolean configIsValid(SpleefConfig config) {
        return false;
    }
    
    @Override
    protected InputStream getDefaultResourceStream() {
        return SpleefStorageUtil.class.getResourceAsStream("defaultSpleefConfig.json");
    }
    
    public Location getStartingLocation() {
        return spleefConfig.startingLocation.toLocation(Bukkit.getWorld(spleefConfig.world));
    }
    
    public World getWorld() {
        return Bukkit.getWorld(spleefConfig.world);
    }
}
