package org.braekpo1nt.mctmanager.games.game.parkourpathway.config;

import com.google.common.base.Preconditions;
import lombok.*;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.validation.Validatable;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.TeamSpawn;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.puzzle.Puzzle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
class ParkourPathwayConfigDTO implements Validatable {
    
    private String version;
    private String world;
    /**
     * the larger glass barrier meant to close off all participants from the puzzles until it is time to race. If null, no glass barrier will be created.
     */
    private @Nullable BoundingBox glassBarrier;
    /**
     * The chat message sent to all participants when the glass barrier opens. Null means no message will be sent.
     */
    private @Nullable Component glassBarrierOpenMessage;
    /**
     * the list of team spawn locations. If null, the team spawn phase will be skipped. Each {@link TeamSpawnDTO#getBarrierArea()} and {@link TeamSpawnDTO#getSpawn()} must be contained in the inBounds area of the first puzzle.
     */
    private @Nullable List<TeamSpawnDTO> teamSpawns;
    /**
     * The chat message sent to all participants when the team spawns open. Null means no message will be sent.
     */
    private @Nullable Component teamSpawnsOpenMessage;
    /** the list of puzzles for this parkour game */
    private List<PuzzleDTO> puzzles;
    private @Nullable BoundingBox spectatorArea;
    private @Nullable List<Material> preventInteractions;
    /**
     * Defines the number of skips and the item used to trigger them
     */
    private @Nullable Skips skips;
    private Scores scores;
    private Durations durations;
    private Component description;
    
    @Data
    static class Skips {
        /** the number of skips each player gets. 0 or negative means no skips. */
        private int numOfSkips;
        /** the item that players interact with to use their skips. Defaults to lapis lazuli */
        private @Nullable Material item;
        /** the display name of the skip item. Defaults to "Skip Puzzle" */
        private @Nullable Component itemName;
        /** the lore of the skip item. Defaults to "Right click to skip the current puzzle" */
        private @Nullable List<Component> itemLore;
        
        public @NotNull Material getItem() {
            return (item != null) ? item : Material.LAPIS_LAZULI;
        }
        
        public @NotNull Component getItemName() {
            return (itemName != null) ? itemName : Component.text("Skip Puzzle");
        }
        
        public @NotNull List<Component> getItemLore() {
            return (itemLore != null) ? itemLore : List.of(
                    Component.text("Right click to skip"),
                    Component.text("the current puzzle")
            );
        }
    }
    
    @Override
    public void validate(@NotNull Validator validator) {
        validator.notNull(this.getVersion(), "version");
        validator.validate(Main.VALID_CONFIG_VERSIONS.contains(this.getVersion()), "invalid config version (%s)", this.getVersion());
        validator.notNull(Bukkit.getWorld(this.getWorld()), "Could not find world \"%s\"", this.getWorld());
        if (spectatorArea != null) {
            BoundingBox spectatorArea = this.spectatorArea;
            validator.validate(spectatorArea.getVolume() >= 1.0, "spectatorArea (%s) volume (%s) must be at least 1.0", spectatorArea, spectatorArea.getVolume());
        }
        validator.notNull(this.getScores(), "scores");
        validator.notNull(this.getScores().getCheckpoint(), "scores.checkpoint");
        validator.validate(this.getScores().getCheckpoint().length >= 2, "scores.checkpoint must have at least two elements");
        validator.notNull(this.getScores().getWin(), "scores.win");
        validator.validate(this.getScores().getWin().length >= 2, "scores.win must have at least two elements");
        validator.notNull(this.getDurations(), "durations");
        validator.validate(this.getDurations().getStarting() >= 0, "durations.starting (%s) can't be negative", this.getDurations().getStarting());
        validator.validate(this.getDurations().getTimeLimit() >= 2, "durations.timeLimit (%s) can't be less than 2", this.getDurations().getTimeLimit());
        validator.validate(this.getDurations().getCheckpointCounter() >= 1, "durations.checkpointCounter (%s) can't be less than 1", this.getDurations().getCheckpointCounter());
        validator.validate(this.getDurations().getCheckpointCounterAlert() >= 1 && this.getDurations().getCheckpointCounter() >= this.getDurations().getCheckpointCounterAlert(), "durations.checkpointCounterAlert (%s) can't be less than 0 or greater than durations.checkpointCounter", this.getDurations().getCheckpointCounterAlert());
    
        validator.notNull(this.getPuzzles(), "puzzles");
        validator.validate(this.getPuzzles().size() >= 3, "puzzles must have at least 3 puzzles");
        validatePuzzles(validator);
    
        if (this.getTeamSpawns() != null) {
            validateTeamSpawns(validator);
        }
        validator.notNull(this.getDescription(), "description");
    }
    
    private void validatePuzzles(Validator validator) {
        for (int i = 0; i < puzzles.size(); i++) {
            PuzzleDTO puzzle = puzzles.get(i);
            validator.notNull(puzzle, "puzzles[%s]", i);
            puzzle.validate(validator.path("puzzles[%d]", i));
            if (i - 1 >= 0) {
                PuzzleDTO previousPuzzle = puzzles.get(i - 1);
                for (int j = 0 ; j < puzzle.getCheckPoints().size(); j++) {
                    PuzzleDTO.CheckPointDTO checkPoint = puzzle.getCheckPoints().get(j);
                    validator.validate(previousPuzzle.isInBounds(checkPoint.getDetectionArea()), "at least one entry in puzzles[%s].inBounds must contain puzzles[%s].checkPoints[%s].detectionArea", i - 1, i, j);
                }
            }
        }
    }
    
    private void validateTeamSpawns(Validator validator) {
        if (this.teamSpawns == null) {
            return;
        }
        PuzzleDTO firstPuzzle = puzzles.getFirst();
        validator.validate(!teamSpawns.isEmpty(), "teamSpawns must have at least one entry");
        for (int i = 0; i < teamSpawns.size(); i++) {
            TeamSpawnDTO teamSpawnDTO = teamSpawns.get(i);
            validator.notNull(teamSpawnDTO, "teamSpawns[%s]", i);
            teamSpawnDTO.validate(validator.path("teamSpawns[%d]", i));
            validator.validate(firstPuzzle.isInBounds(teamSpawnDTO.getBarrierArea()), "teamSpawns[%d].barrierArea must be contained in at least one of the inBounds boxes of puzzles[0]", i);
            validator.validate(firstPuzzle.isInBounds(teamSpawnDTO.getSpawn().toVector()), "teamSpawns[%d].spawn must be contained in at least one of the inBounds boxes of puzzles[0]", i);
        }
    }
    
    ParkourPathwayConfig toConfig() {
        World newWorld = Bukkit.getWorld(this.getWorld());
        Preconditions.checkArgument(newWorld != null, "Could not find world \"%s\"", this.getWorld());
        BoundingBox newGlassBarrier = null;
        if (this.getGlassBarrier() != null) {
            newGlassBarrier = this.getGlassBarrier();
        }
        List<TeamSpawn> newTeamSpawns = null;
        if (this.getTeamSpawns() != null) {
            newTeamSpawns = TeamSpawnDTO.toTeamSpawns(newWorld, this.getTeamSpawns());
        }
        List<Puzzle> newPuzzles = PuzzleDTO.toPuzzles(newWorld, this.getPuzzles());
        Location newStartingLocation = newPuzzles.getFirst().checkPoints().getFirst().respawn();
        
        ParkourPathwayConfig.ParkourPathwayConfigBuilder builder = ParkourPathwayConfig.builder()
                .world(newWorld)
                .startingLocation(newStartingLocation)
                .spectatorArea(this.spectatorArea)
                .teamSpawns(newTeamSpawns)
                .puzzles(newPuzzles)
                .glassBarrier(newGlassBarrier)
                .glassBarrierOpenMessage(this.glassBarrierOpenMessage)
                .teamSpawnsOpenMessage(this.teamSpawnsOpenMessage)
                .startingDuration(this.durations.starting)
                .timeLimitDuration(this.durations.timeLimit)
                .teamSpawnsDuration(this.durations.teamSpawn)
                .mercyRuleDuration(this.durations.checkpointCounter)
                .mercyRuleAlertDuration(this.durations.checkpointCounterAlert)
                .checkpointScore(this.scores.checkpoint)
                .winScore(this.scores.win)
                .preventInteractions(this.preventInteractions != null ? this.preventInteractions : Collections.emptyList())
                .descriptionDuration(this.durations.description)
                .description(this.description);
        
        if (this.skips != null && this.skips.getNumOfSkips() > 0) {
            ItemStack skipItem = new ItemStack(this.skips.getItem());
            skipItem.editMeta(meta -> {
                meta.displayName(this.skips.getItemName());
                meta.lore(this.skips.getItemLore());
            });
            builder
                    .numOfSkips(this.skips.getNumOfSkips())
                    .skipItem(skipItem);
        } else {
            builder
                    .numOfSkips(0)
                    .skipItem(new ItemStack(Material.LAPIS_LAZULI));
        }
        
        return builder.build();
    }
    
    public static ParkourPathwayConfigDTO fromConfig(ParkourPathwayConfig config) {
        return ParkourPathwayConfigDTO.builder()
                .version(Main.VALID_CONFIG_VERSIONS.getLast())
                .world(config.getWorld().getName())
                .glassBarrier(config.getGlassBarrier())
                .glassBarrierOpenMessage(config.getGlassBarrierOpenMessage())
                .teamSpawns(config.getTeamSpawns() != null ? TeamSpawnDTO.fromTeamSpawns(config.getTeamSpawns()) : null)
                .teamSpawnsOpenMessage(config.getTeamSpawnsOpenMessage())
                .puzzles(PuzzleDTO.fromPuzzles(config.getPuzzles()))
                .spectatorArea(config.getSpectatorArea())
                .scores(new Scores(config.getCheckpointScore(), config.getWinScore()))
                .preventInteractions(config.getPreventInteractions())
                .durations(new Durations(config.getTeamSpawnsDuration(), config.getStartingDuration(), config.getTimeLimitDuration(), config.getMercyRuleDuration(), config.getMercyRuleAlertDuration(), config.getDescriptionDuration()))
                .description(config.getDescription())
                .build();
    }
    
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class Scores {
        /**
         * points for reaching puzzle checkpoints. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
         */
        private int[] checkpoint;
        /**
         * points for winning. for x elements, nth score will be awarded unless n is greater than or equal to x in which case the xth score will be awarded 
         */
        private int[] win;
    }
    
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class Durations {
        /**
         * how long the teams spend inside their individual spawns. Defaults to -1.
         */
        private int teamSpawn = -1;
        /**
         * how long (after the teamSpawn duration is over) until the final glass barrier is dropped
         */
        private int starting;
        private int timeLimit;
        private int checkpointCounter;
        private int checkpointCounterAlert;
        private int description = 0;
    }
    
}
