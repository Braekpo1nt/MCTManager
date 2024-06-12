package org.braekpo1nt.mctmanager.hub.config;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        Leaderboard leaderboard,
        double yLimit, 
        Durations durations) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version(), "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version()), "invalid config version (%s)", this.version());
        validator.notNull(Bukkit.getWorld(this.world()), "Could not find world \"%s\"", this.world());
        validator.notNull(this.spawn(), "spawn");
        validator.notNull(this.podium(), "podium");
        if (leaderboard != null) {
            if (this.leaderBoard() == null) {
                validator.validate(leaderboard.getLocation() != null, "leaderboard.location can't be null if leaderBoard (uppercase \"B\") is null");
            }
            leaderboard.validate(validator.path("leaderboard"));
        } else {
            validator.validate(leaderBoard != null, "leaderBoard can't be null if leaderboard (lowercase \"b\") is null");
        }
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
        Location newLeaderboardLocation;
        int newTopNumber = 10;
        if (this.leaderboard != null) {
            if (this.leaderboard.location != null) {
                newLeaderboardLocation = this.leaderboard.getLocation().toLocation(newWorld);
            } else {
                newLeaderboardLocation = this.leaderBoard.toLocation(newWorld);
            }
            newTopNumber = this.leaderboard.getTopPlayers();
        } else {
            newLeaderboardLocation = this.leaderBoard.toLocation(newWorld);
        }
        
        return HubConfig.builder()
                .world(newWorld)
                .spawn(this.spawn.toLocation(newWorld))
                .podium(this.podium.toLocation(newWorld))
                .podiumObservation(this.podiumObservation.toLocation(newWorld))
                .leaderboardLocation(newLeaderboardLocation)
                .topPlayers(newTopNumber)
                .yLimit(this.yLimit)
                .tpToHubDuration(this.durations.tpToHub)
                .build();
    }
    
    record Durations(int tpToHub) {
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Leaderboard implements Validatable {
        private LocationDTO location;
        /**
         * how many players should be shown (i.e. top 10, top 20, top 5, etc.)
         */
        private int topPlayers = 10;
        
        @Override
        public void validate(@NotNull Validator validator) {
            // location can be null if the external HubConfigDTO#leaderBoard Vector is not null, so that validation is left up to external validation
            validator.validate(topPlayers >= 0, "topNumber can't be negative");
        }
        
    }
}
