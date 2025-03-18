package org.braekpo1nt.mctmanager.games.game.footrace.config;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

record FootRaceConfigDTO(
        String version, 
        String world, 
        int laps, // must be at least 1 lap. for backwards compatibility, excluding this or putting a number less than 1 results in 1 lap in-game, rather than a validation error
        LocationDTO startingLocation, 
        List<BoundingBox> checkpoints,
        @Nullable BoundingBox spectatorArea, 
        BoundingBox glassBarrier,
        @Nullable List<Material> preventInteractions,
        Scores scores, 
        Durations durations, 
        Component description) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version(), "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version()), "invalid config version (%s)", this.version());
        validator.notNull(this.world(), "world");
        validator.notNull(Bukkit.getWorld(this.world()), "Could not find world \"%s\"", this.world());
        validator.notNull(this.startingLocation(), "startingLocation");
        validator.notNull(this.checkpoints, "checkpoints");
        validator.validate(this.checkpoints.size() >= 2, "checkpoints must have at least 2 elements");
        List<BoundingBox> realCheckpoints = new ArrayList<>(this.checkpoints.size());
        for (int i = 0; i < this.checkpoints.size(); i++) {
            BoundingBox checkpoint = this.checkpoints.get(i);
            validator.notNull(checkpoint, "checkpoints[%d]", i);
            validator.validate(checkpoint.getVolume() >= 1.0, "checkpoints[%d]'s volume must be at least 1.0", i);
            realCheckpoints.add(checkpoint);
        }
        for (int i = 0; i < realCheckpoints.size(); i++) {
            BoundingBox checkpoint = realCheckpoints.get(i);
            for (int j = i + 1; j < realCheckpoints.size(); j++) {
                BoundingBox other = realCheckpoints.get(j);
                validator.validate(!checkpoint.overlaps(other), "checkpoints[%d] can't overlap checkpoints[%d]", i, j);
            }
        }
        if (spectatorArea != null) {
            validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        }
        validator.notNull(this.glassBarrier(), "glassBarrier");
        validator.notNull(this.scores(), "scores");
        validator.notNull(this.scores().placementPoints(), "placementPoints");
        validator.validate(this.scores().placementPoints().length >= 1, "placementPoints must have at least one entry");
        validator.notNull(this.durations(), "durations");
        validator.validate(this.durations().getStartRace() >= 0, "durations.startRace (%s) can't be negative", this.durations().getStartRace());
        validator.validate(this.durations().getRaceEndCountdown() >= 0, "durations.raceEndCountdown (%s) can't be negative", this.durations().getRaceEndCountdown());
        validator.notNull(this.description(), "description");
    }
    
    record Scores(int completeLap, int[] placementPoints, int detriment) {
        
    }
    
    @Data
    @AllArgsConstructor
    static class Durations {
        private int startRace;
        private int raceEndCountdown;
        private int description;
        /**
         * The time (in seconds) that the game remains in the "Game Over" stage until 
         * returning to hub. Defaults to 10. 
         */
        private int gameOver = 10;
    }
    
    FootRaceConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        
        return FootRaceConfig.builder()
                .world(newWorld)
                .startingLocation(this.startingLocation.toLocation(newWorld))
                .laps(Math.max(this.laps, 1))
                .glassBarrier(this.glassBarrier)
                .completeLapScore(this.scores.completeLap)
                .placementPoints(this.scores.placementPoints)
                .detriment(this.scores.detriment)
                .startRaceDuration(this.durations.startRace)
                .raceEndCountdownDuration(this.durations.raceEndCountdown)
                .descriptionDuration(this.durations.description)
                .gameOverDuration(this.durations.gameOver)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .spectatorArea(this.spectatorArea)
                .checkpoints(new ArrayList<>(this.checkpoints))
                .description(this.description)
                .build();
    }
    
    public static FootRaceConfigDTO fromConfig(@NotNull FootRaceConfig config) {
        return new FootRaceConfigDTO(
                Main.VALID_CONFIG_VERSIONS.getLast(),
                config.getWorld().getName(),
                config.getLaps(),
                LocationDTO.from(config.getStartingLocation()),
                config.getCheckpoints(),
                config.getSpectatorArea(),
                config.getGlassBarrier(),
                config.getPreventInteractions(),
                new Scores(
                        config.getCompleteLapScore(),
                        config.getPlacementPoints(),
                        config.getDetriment()
                ),
                new Durations(
                        config.getStartRaceDuration(),
                        config.getRaceEndCountdownDuration(),
                        config.getDescriptionDuration(),
                        config.getGameOverDuration()
                ),
                config.getDescription()
        );
    }
    
}
