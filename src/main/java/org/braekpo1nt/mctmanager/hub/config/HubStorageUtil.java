package org.braekpo1nt.mctmanager.hub.config;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.config.GameConfigStorageUtil;
import org.braekpo1nt.mctmanager.games.game.config.LocationDTO;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

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
    public boolean loadConfig() throws IllegalArgumentException {
        if (!configFile.exists()) {
            HubConfig defaultConfig = createDefaultConfig();
            Bukkit.getLogger().warning(String.format("%s not found, creating default %s.", configFile, configFileName));
            saveConfig(defaultConfig);
        }
        return super.loadConfig();
    }
    
    /**
     * @return a default config with the first world in the list of `Bukkit.getWorld()` and the spawn as all the locations. 
     * @throws IllegalArgumentException if no worlds exist in the server
     */
    @NotNull
    protected HubConfig createDefaultConfig() {
        Optional<World> optionalWorld = Bukkit.getWorlds().stream().findFirst();
        Preconditions.checkArgument(optionalWorld.isPresent(), "No worlds exist in server.");
        World defaultWorld = optionalWorld.get();
        Location defaultSpawn = defaultWorld.getSpawnLocation();
        LocationDTO defaultLocation = new LocationDTO(defaultSpawn);
        int yLimit = -64;
        HubConfig.Durations durations = new HubConfig.Durations(10);
        return new HubConfig(Main.CONFIG_VERSION, defaultWorld.getName(), defaultLocation, defaultLocation, defaultLocation, defaultSpawn.toVector(), yLimit, durations);
    }
    
    @Override
    protected boolean configIsValid(@Nullable HubConfig config) throws IllegalArgumentException {
        Preconditions.checkArgument(config != null, "Saved config is null");
        Preconditions.checkArgument(config.version() != null, "version can't be null");
        Preconditions.checkArgument(config.version().equals(Main.CONFIG_VERSION), "Config version %s not supported. %s required.", config.version(), Main.CONFIG_VERSION);
        Preconditions.checkArgument(Bukkit.getWorld(config.world()) != null, "Could not find world \"%s\"", config.world());
        Preconditions.checkArgument(config.spawn() != null, "spawn can't be null");
        Preconditions.checkArgument(config.podium() != null, "podium can't be null");
        Preconditions.checkArgument(config.leaderBoard() != null, "leaderBoard can't be null");
        Preconditions.checkArgument(config.yLimit() < config.spawn().getY(), "yLimit (%s) must be less than spawn.y (%s)", config.yLimit(), config.spawn().getY());
        Preconditions.checkArgument(config.yLimit() < config.podium().getY(), "yLimit (%s) must be less than podium.y (%s)", config.yLimit(), config.podium().getY());
        Preconditions.checkArgument(config.yLimit() < config.podiumObservation().getY(), "yLimit (%s) must be less than podiumObservation.y (%s)", config.yLimit(), config.podiumObservation().getY());
        Preconditions.checkArgument(config.yLimit() < config.podiumObservation().getY(), "yLimit (%s) must be less than podiumObservation.y (%s)", config.yLimit(), config.podiumObservation().getY());
        Preconditions.checkArgument(config.durations() != null, "durations can't be null");
        Preconditions.checkArgument(config.durations().tpToHub() > 0, "durations.tpToHub must be greater than 0");
        return true;
    }
    
    @Override
    protected void setConfig(HubConfig config) throws IllegalArgumentException {
        World newWorld = Bukkit.getWorld(config.world());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", config.world());
        Location newSpawn = config.spawn().toLocation(newWorld);
        Location newPodium = config.podium().toLocation(newWorld);
        Location newPodiumObservation = config.podiumObservation().toLocation(newWorld);
        Location newLeaderBoard = config.leaderBoard().toLocation(newWorld);
        
        //now that we know everything is valid, store the real values
        this.world = newWorld;
        this.spawn = newSpawn;
        this.podium = newPodium;
        this.podiumObservation = newPodiumObservation;
        this.leaderBoard = newLeaderBoard;
        this.hubConfig = config;
    }
    
    @Override
    protected InputStream getExampleResourceStream() {
        return HubStorageUtil.class.getResourceAsStream("exampleHubConfig.json");
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
    
    public double getYLimit() {
        return hubConfig.yLimit();
    }
}
