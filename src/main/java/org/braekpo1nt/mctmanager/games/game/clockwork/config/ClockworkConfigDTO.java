package org.braekpo1nt.mctmanager.games.game.clockwork.config;


import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.dto.net.kyori.adventure.sound.SoundDTO;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.clockwork.Chaos;
import org.braekpo1nt.mctmanager.games.game.clockwork.Wedge;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

record ClockworkConfigDTO(
        String version, 
        String world, 
        Vector startingLocation,
        @Nullable BoundingBox spectatorArea, 
        Chaos chaos, 
        List<WedgeDTO> wedges, 
        int rounds, 
        SoundDTO clockChime, 
        double initialChimeInterval, 
        double chimeIntervalDecrement, 
        Team.OptionStatus collisionRule,
        @Nullable List<Material> preventInteractions,
        Scores scores, 
        Durations durations,
        Component getToWedgeMessage,
        Component description) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version, "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.notNull(Bukkit.getWorld(this.world), "Could not find world \"%s\"", this.world);
        validator.notNull(this.startingLocation, "startingLocation");
        if (spectatorArea != null) {
            validator.validate(this.spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        }
        this.chaos.validate(validator.path("chaos"));
        validator.notNull(this.wedges, "wedges");
        validator.validate(this.wedges.size() == 12, "wedges must have 12 entries");
        for (ClockworkConfigDTO.WedgeDTO wedgeDTO : this.wedges) {
            validator.notNull(wedgeDTO, "wedge");
            validator.notNull(wedgeDTO.detectionArea(), "wedge.detectionArea");
            validator.validate(wedgeDTO.detectionArea().getVolume() >= 1.0, "wedge.detectionArea (%s) volume (%s) must be at least 1.0", wedgeDTO.detectionArea(), wedgeDTO.detectionArea().getVolume());
        }
        validator.validate(this.rounds >= 1, "rounds must be at least 1");
        validator.notNull(this.clockChime, "clockChime");
        this.clockChime.validate(validator.path("clockChime"));
        validator.validate(this.initialChimeInterval >= 0, "initialChimeInterval can't be negative");
        validator.validate(this.chimeIntervalDecrement >= 0, "chimeIntervalDecrement can't be negative");
        validator.validate(this.chimeIntervalDecrement > 0, "chimeIntervalDecrement (%s) can't be greater than initialChimeInterval (%s)", this.chimeIntervalDecrement, this.initialChimeInterval);
        validator.notNull(this.collisionRule, "collisionRule");
        validator.notNull(this.scores, "scores");
        validator.notNull(this.durations, "durations");
        validator.validate(this.durations.breather() >= 0, "durations.breather can't be negative");
        validator.validate(this.durations.getToWedge() >= 0, "durations.getToWedge can't be negative");
        validator.validate(this.durations.stayOnWedge() >= 0, "durations.stayOnWedge can't be negative");
        validator.notNull(this.description, "description");
    }
    
    public ClockworkConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        List<Wedge> newWedges = new ArrayList<>(this.wedges.size());
        for (ClockworkConfigDTO.WedgeDTO wedgeDTO : this.wedges) {
            newWedges.add(new Wedge(wedgeDTO.detectionArea()));
        }
        return ClockworkConfig.builder()
                .world(newWorld)
                .startingLocation(this.startingLocation.toLocation(newWorld))
                .rounds(this.rounds)
                .playerEliminationScore(this.scores.playerElimination)
                .teamEliminationScore(this.scores.teamElimination)
                .winRoundScore(this.scores.winRound)
                .breatherDuration(this.durations.breather)
                .getToWedgeDuration(this.durations.getToWedge)
                .stayOnWedgeDuration(this.durations.stayOnWedge)
                .initialChimeInterval(this.initialChimeInterval)
                .chimeIntervalDecrement(this.chimeIntervalDecrement)
                .clockChimeSound(this.clockChime.getKey())
                .clockChimeVolume(this.clockChime.getVolume())
                .clockChimePitch(this.clockChime.getPitch())
                .chaos(this.chaos)
                .collisionRule(this.collisionRule)
                .wedges(newWedges)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .descriptionDuration(this.durations.description)
                .spectatorArea(this.spectatorArea)
                .getToWedgeMessage(getToWedgeMessage)
                .description(this.description)
                .build();
    }
    
    record WedgeDTO(BoundingBox detectionArea) {
    }
    
    record Scores(int playerElimination, int teamElimination, int winRound) {
    }
    
    record Durations(int breather, int getToWedge, int stayOnWedge, int description) {
    }
    
}
