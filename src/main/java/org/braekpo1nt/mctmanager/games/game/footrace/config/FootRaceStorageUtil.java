package org.braekpo1nt.mctmanager.games.game.footrace.config;

import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class FootRaceStorageUtil extends GameConfigStorageUtil<FootRaceConfig> {
    private FootRaceConfig footRaceConfig;
    private World world;
    private Location startingLocation;
    
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
    
    public World getWorld() {
        return world;
    }
    
    public Location getStartingLocation() {
        return startingLocation;
    }
    
    public FootRaceConfig.Scores getScores() {
        return footRaceConfig.scores();
    }
    
    public BoundingBox getFinishLine() {
        return footRaceConfig.getFinishLine();
    }
}
