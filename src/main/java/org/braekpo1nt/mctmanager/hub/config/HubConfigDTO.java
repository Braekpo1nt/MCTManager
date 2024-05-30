package org.braekpo1nt.mctmanager.hub.config;

import com.google.common.base.Preconditions;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

record HubConfigDTO(
        String version, 
        String world, 
        LocationDTO spawn, 
        LocationDTO podium, 
        LocationDTO podiumObservation, 
        Vector leaderBoard, 
        double yLimit, 
        Durations durations) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version(), "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version()), "invalid config version (%s)", this.version());
        validator.notNull(Bukkit.getWorld(this.world()), "Could not find world \"%s\"", this.world());
        validator.notNull(this.spawn(), "spawn");
        validator.notNull(this.podium(), "podium");
        validator.notNull(this.leaderBoard(), "leaderBoard");
        validator.validate(this.yLimit() < this.spawn().getY(), "yLimit (%s) must be less than spawn.y (%s)", this.yLimit(), this.spawn().getY());
        validator.validate(this.yLimit() < this.podium().getY(), "yLimit (%s) must be less than podium.y (%s)", this.yLimit(), this.podium().getY());
        validator.validate(this.yLimit() < this.podiumObservation().getY(), "yLimit (%s) must be less than podiumObservation.y (%s)", this.yLimit(), this.podiumObservation().getY());
        validator.validate(this.yLimit() < this.podiumObservation().getY(), "yLimit (%s) must be less than podiumObservation.y (%s)", this.yLimit(), this.podiumObservation().getY());
        validator.notNull(this.durations(), "durations");
        validator.validate(this.durations().tpToHub() > 0, "durations.tpToHub must be greater than 0");
    }
    
    HubConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        return HubConfig.builder()
                .world(newWorld)
                .spawn(this.spawn.toLocation(newWorld))
                .podium(this.podium.toLocation(newWorld))
                .podiumObservation(this.podiumObservation.toLocation(newWorld))
                .leaderBoard(this.leaderBoard)
                .yLimit(this.yLimit)
                .tpToHubDuration(this.durations.tpToHub)
                .build();
    }
    
    record Durations(int tpToHub) {
    }
}
