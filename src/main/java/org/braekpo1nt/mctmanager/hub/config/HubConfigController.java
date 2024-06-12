package org.braekpo1nt.mctmanager.hub.config;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

public class HubConfigController extends ConfigController<HubConfigDTO> {
    
    private final File configFile;
    
    public HubConfigController(File configDirectory) {
        this.configFile = new File(configDirectory, "hubConfig.json");
    }
    
    /**
     * Gets the config from storage 
     * @return the config for spleef
     * @throws ConfigInvalidException if the config is invalid
     * @throws ConfigIOException if there is an IO problem getting the config
     */
    public @NotNull HubConfig getConfig() throws ConfigException {
        HubConfigDTO configDTO = loadConfigDTO(configFile, HubConfigDTO.class);
        configDTO.validate(new Validator("hubConfig"));
        return configDTO.toConfig();
    }
    
    /**
     * @return the default config, guaranteed to enable the operation of the plugin without throwing exceptions,
     * but will be mostly nonsense (all locations will be the spawn of the first world in the list of worlds,
     * for instance) 
     * @throws IllegalStateException if the server has 0 worlds
     */
    public @NotNull HubConfig getDefaultConfig() {
        HubConfigDTO configDTO = createDefaultConfig();
        return configDTO.toConfig();
    }
    
    @Override
    public @NotNull HubConfigDTO loadConfigDTO(@NotNull File configFile, @NotNull Class<HubConfigDTO> configType) throws ConfigInvalidException, ConfigIOException {
        if (!configFile.exists()) {
            HubConfigDTO defaultConfigDTO = createDefaultConfig();
            Bukkit.getLogger().warning(String.format("hubConfig.json not found, creating default: %s.", configFile));
            saveConfigDTO(defaultConfigDTO, configFile);
        }
        return super.loadConfigDTO(configFile, configType);
    }
    
    /**
     * @return a default config with the first world in the list of `Bukkit.getWorld()` and the spawn as all the locations. 
     * @throws IllegalArgumentException if no worlds exist in the server
     */
    @NotNull protected HubConfigDTO createDefaultConfig() {
        Optional<World> optionalWorld = Bukkit.getWorlds().stream().findFirst();
        Preconditions.checkState(optionalWorld.isPresent(), "No worlds exist in server.");
        World defaultWorld = optionalWorld.get();
        Location defaultSpawn = defaultWorld.getSpawnLocation();
        LocationDTO defaultLocation = new LocationDTO(defaultSpawn);
        int yLimit = -64;
        HubConfigDTO.Durations durations = new HubConfigDTO.Durations(10);
        return new HubConfigDTO(Main.VALID_CONFIG_VERSIONS.get(Main.VALID_CONFIG_VERSIONS.size() - 1), defaultWorld.getName(), defaultLocation, defaultLocation, defaultLocation, defaultSpawn.toVector(), new HubConfigDTO.Leaderboard(defaultLocation, 10), yLimit, durations);
    }
}
