package org.braekpo1nt.mctmanager.hub.config;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

public class HubStorageUtil extends GameConfigStorageUtil<HubConfig> {
    
    private HubConfig hubConfig;
    private World world;
    private Location spawn;
    private Location podium;
    private Location podiumObservation;
    private Location leaderBoard;
    
    /**
     * @param configDirectory The directory that the config should be located in (e.g. the plugin's data folder)
     */
    public HubStorageUtil(File configDirectory) {
        super(configDirectory, "hubConfig.json", HubConfig.class);
    }
    
    @Override
    protected HubConfig getConfig() {
        return hubConfig;
    }
    
    @Override
    protected boolean configIsValid(@Nullable HubConfig config) throws IllegalArgumentException {
        return false;
    }
    
    @Override
    protected void setConfig(HubConfig config) throws IllegalArgumentException {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        Location newSpawn = config.spawn().toLocation(newWorld);
        Location newPodium = config.podium().toLocation(newWorld);
        Location newPodiumObservation = config.podiumObservation().toLocation(newWorld);
        Location newLeaderBoard = config.leaderBoard().toLocation(newWorld);
        
        //now that we know everything is valid
        this.world = newWorld;
        this.spawn = newSpawn;
        this.podium = newPodium;
        this.podiumObservation = newPodiumObservation;
        this.leaderBoard = newLeaderBoard;
        this.hubConfig = config;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return null;
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getSpawn() {
        return spawn;
    }
    
    public Location getPodium() {
        return podium;
    }
    
    public Location getPodiumObservation() {
        return podiumObservation;
    }
    
    public Location getLeaderBoard() {
        return leaderBoard;
    }
}
