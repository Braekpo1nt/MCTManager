package org.braekpo1nt.mctmanager.games.game.footrace.config;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.LocationDTO;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
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
        LocationDTO startingLocation, 
        @Nullable BoundingBoxDTO finishLine,
        @Nullable List<BoundingBoxDTO> checkpoints,
        @Nullable BoundingBoxDTO spectatorArea, 
        BoundingBoxDTO glassBarrier,
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
        if (this.checkpoints == null) {
            validator.validate(this.finishLine != null, "finishLine can't be null if checkpoints is null");
            BoundingBox finishLine = this.finishLine.toBoundingBox();
            validator.validate(finishLine.getVolume() >= 1.0, "finishLine's volume (%s) can't be less than 1. %s", finishLine.getVolume(), finishLine);
            validator.validate(!finishLine.contains(this.startingLocation().toVector()), "startingLocation (%S) can't be inside finishLine (%S)", this.startingLocation(), finishLine);
        } else {
            validator.validate(this.checkpoints.size() >= 2, "checkpoints must have at least 2 elements");
            List<BoundingBox> realCheckpoints = new ArrayList<>(this.checkpoints.size());
            for (int i = 0; i < this.checkpoints.size(); i++) {
                BoundingBoxDTO checkpointDTO = this.checkpoints.get(i);
                validator.notNull(checkpointDTO, "checkpoints[%d]", i);
                BoundingBox checkpoint = checkpointDTO.toBoundingBox();
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
        }
        if (spectatorArea != null) {
            BoundingBox spectatorArea = this.spectatorArea.toBoundingBox();
            validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        }
        validator.notNull(this.glassBarrier(), "glassBarrier");
        validator.notNull(this.scores(), "scores");
        validator.notNull(this.scores().placementPoints(), "placementPoints");
        validator.validate(this.scores().placementPoints().length >= 1, "placementPoints must have at least one entry");
        validator.notNull(this.durations(), "durations");
        validator.validate(this.durations().startRace() >= 0, "durations.startRace (%s) can't be negative", this.durations().startRace());
        validator.validate(this.durations().raceEndCountdown() >= 0, "durations.raceEndCountdown (%s) can't be negative", this.durations().raceEndCountdown());
        validator.notNull(this.description(), "description");
    }
    
    record Scores(int completeLap, int[] placementPoints, int detriment) {
        
    }
    record Durations(int startRace, int raceEndCountdown, int description, int end) {
        
    }
    FootRaceConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        
        FootRaceConfig.FootRaceConfigBuilder builder = FootRaceConfig.builder()
                .world(newWorld)
                .startingLocation(this.startingLocation.toLocation(newWorld))
                .glassBarrier(this.glassBarrier.toBoundingBox())
                .completeLapScore(this.scores.completeLap)
                .placementPoints(this.scores.placementPoints)
                .detriment(this.scores.detriment)
                .startRaceDuration(this.durations.startRace)
                .raceEndCountdownDuration(this.durations.raceEndCountdown)
                .descriptionDuration(this.durations.description)
                .endDuration(this.durations.end)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .spectatorArea(this.spectatorArea != null ? this.spectatorArea.toBoundingBox() : null)
                .description(this.description);
        if (this.checkpoints == null) {
            Preconditions.checkState(this.finishLine != null, "finishLine can't be null if checkpoints is null");
            builder
                    .finishLine(this.finishLine.toBoundingBox())
                    .useLegacy(true);
        } else {
            builder
                    .checkpoints(new ArrayList<>(BoundingBoxDTO.toBoundingBoxes(this.checkpoints)))
                    .useLegacy(false);
        }
        return builder.build();
    }
    
    public static FootRaceConfigDTO fromConfig(@NotNull FootRaceConfig config) {
        return new FootRaceConfigDTO(
                Main.VALID_CONFIG_VERSIONS.get(Main.VALID_CONFIG_VERSIONS.size() - 1),
                config.getWorld().getName(),
                LocationDTO.from(config.getStartingLocation()),
                config.getFinishLine() != null ? BoundingBoxDTO.from(config.getFinishLine()) : null,
                BoundingBoxDTO.from(config.getCheckpoints()),
                config.getSpectatorArea() != null ? BoundingBoxDTO.from(config.getSpectatorArea()) : null,
                BoundingBoxDTO.from(config.getGlassBarrier()),
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
                        config.getEndDuration()
                ),
                config.getDescription()
        );
    }
    
}
