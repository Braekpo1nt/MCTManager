package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class CaptureTheFlagStorageUtil extends GameConfigStorageUtil<CaptureTheFlagConfig> {
    private CaptureTheFlagConfig captureTheFlagConfig = null;
    private World world;
    private Location spawnObservatory;
    
    public CaptureTheFlagStorageUtil(File configDirectory) {
        super(configDirectory, "captureTheFlagConfig.json", CaptureTheFlagConfig.class);
    }
    
    @Override
    protected CaptureTheFlagConfig getConfig() {
        return captureTheFlagConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable CaptureTheFlagConfig config) throws IllegalArgumentException {
        return false;
    }
    
    @Override
    protected void setConfig(CaptureTheFlagConfig config) {
        this.captureTheFlagConfig = config;
        world = Bukkit.getWorld(captureTheFlagConfig.world());
        if (world == null) {
            throw new IllegalArgumentException("world can't be null");
        }
        spawnObservatory = captureTheFlagConfig.spawnObservatory().toLocation(world);
        
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return CaptureTheFlagStorageUtil.class.getResourceAsStream("exampleCaptureTheFlagConfig.json");
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getSpawnObservatory() {
        return spawnObservatory;
    }
    
    public int getKillPoints() {
        return captureTheFlagConfig.points().kill();
    }
    
    public int getWinPoints() {
        return captureTheFlagConfig.points().win();
    }
    
    
}
