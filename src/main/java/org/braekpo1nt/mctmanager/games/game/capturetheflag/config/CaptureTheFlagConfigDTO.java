package org.braekpo1nt.mctmanager.games.game.capturetheflag.config;


import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.config.dto.org.bukkit.util.BoundingBoxDTO;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

record CaptureTheFlagConfigDTO(
        String version, 
        String world, 
        Vector spawnObservatory, 
        List<ArenaDTO> arenas, 
        Map<String, LoadoutDTO> loadouts, 
        BoundingBoxDTO spectatorArea, 
        Scores scores, 
        Durations durations, 
        Component description) implements Validatable {
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.version, "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.version), "invalid config version (%s)", this.version);
        validator.validate(Bukkit.getWorld(this.world) != null, "Could not find world \"%s\"", this.world);
        validator.notNull(this.arenas, "arenas");
        validator.validate(this.arenas.size() >= 1, "arenas: there must be at least 1 arena");
        validator.validateList(this.arenas, "arenas");
        validator.notNull(this.spectatorArea, "spectatorArea");
        BoundingBox spectatorArea = this.spectatorArea.toBoundingBox();
        validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        validator.notNull(this.scores, "scores");
        validator.notNull(this.durations, "durations");
        validator.validate(this.durations.matchesStarting() >= 0, "durations.matchesStarting (%s) can't be negative", this.durations.matchesStarting());
        validator.validate(this.durations.classSelection() >= 0, "durations.classSelection (%s) can't be negative", this.durations.classSelection());
        validator.validate(this.durations.roundTimer() >= 0, "durations.roundTimer (%s) can't be negative", this.durations.roundTimer());
        validator.notNull(this.loadouts, "loadouts");
        validator.validate(this.loadouts.size() >= 4, "loadouts must contain at least 4 entries");
        Set<Material> uniqueMenuItems = new HashSet<>();
        for (String battleClass : this.loadouts.keySet()) {
            validator.notNull(battleClass, "loadouts.keys");
            validator.validate(!battleClass.isEmpty() && !battleClass.isBlank(), "loadouts keys can't be empty");
            LoadoutDTO loadout = this.loadouts.get(battleClass);
            validator.validate(!uniqueMenuItems.contains(loadout.getMenuItem()), "loadouts[%s].menuItem %s for BattleClass %s is not unique", battleClass, loadout.getMenuItem(), battleClass);
            uniqueMenuItems.add(loadout.getMenuItem());
            loadout.validate(validator.path("loadouts[%s]", battleClass));
        }
        validator.notNull(this.description, "description");
    }
    
    public CaptureTheFlagConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.world);
        Preconditions.checkState(newWorld != null, "Could not find world \"%s\"", this.world);
        return CaptureTheFlagConfig.builder()
                .world(newWorld)
                .spawnObservatory(this.spawnObservatory.toLocation(newWorld))
                .arenas(ArenaDTO.toArenas(this.arenas, newWorld))
                .loadouts(LoadoutDTO.toLoadouts(this.loadouts))
                .matchesStartingDuration(this.durations.matchesStarting)
                .roundTimerDuration(this.durations.roundTimer)
                .classSelectionDuration(this.durations.classSelection)
                .winScore(this.scores.win)
                .killScore(this.scores.kill)
                .descriptionDuration(this.durations.description)
                .description(this.description)
                .build();
    }
    
    /**
     * Holds the scores for the game
     * @param kill the number of points to award for getting a kill
     * @param win the number of points to award for winning a match
     */
    record Scores(int kill, int win) {
    }
    
    /**
     * Holds durations for the game
     * @param matchesStarting the duration (in seconds) for the "matches starting" period (i.e. waiting in the lobby for the match to start)
     * @param classSelection the duration (in seconds) of the class selection period
     * @param roundTimer the duration (in seconds) of each round
     */
    record Durations(int matchesStarting, int classSelection, int roundTimer, int description) {
    }
    
}
