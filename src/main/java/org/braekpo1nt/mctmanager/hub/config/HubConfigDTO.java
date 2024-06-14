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
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

record HubConfigDTO(
        String version, 
        String world, 
        LocationDTO spawn, 
        LocationDTO podium, 
        LocationDTO podiumObservation,
        List<LeaderboardDTO> leaderboards,
        double yLimit, 
        Durations durations) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version(), "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version()), "invalid config version (%s)", this.version());
        validator.notNull(Bukkit.getWorld(this.world()), "Could not find world \"%s\"", this.world());
        validator.notNull(this.spawn(), "spawn");
        validator.notNull(this.podium(), "podium");
        if (leaderboards != null) {
            validator.validateList(leaderboards, "leaderboards");
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
        return HubConfig.builder()
                .world(newWorld)
                .spawn(this.spawn.toLocation(newWorld))
                .podium(this.podium.toLocation(newWorld))
                .podiumObservation(this.podiumObservation.toLocation(newWorld))
                .leaderboards(LeaderboardDTO.toLeaderboards(this.leaderboards, newWorld))
                .yLimit(this.yLimit)
                .tpToHubDuration(this.durations.tpToHub)
                .build();
    }
    
    record Durations(int tpToHub) {
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class LeaderboardDTO implements Validatable {
        /**
         * the title of the leaderboard. Null will make the leaderboard have no title. 
         */
        private @Nullable String title;
        /**
         * the location the leaderboard should appear
         */
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
        
        HubConfig.Leaderboard toLeaderboard(@NotNull World world) {
            return new HubConfig.Leaderboard(
                    title,
                    location.toLocation(world), 
                    topPlayers
            );
        }
        
        static List<HubConfig.Leaderboard> toLeaderboards(@Nullable List<LeaderboardDTO> leaderboardDTOS, @NotNull World world) {
            if (leaderboardDTOS == null) {
                return Collections.emptyList();
            }
            return leaderboardDTOS.stream().map(l -> l.toLeaderboard(world)).toList();
        }
        
    }
}
