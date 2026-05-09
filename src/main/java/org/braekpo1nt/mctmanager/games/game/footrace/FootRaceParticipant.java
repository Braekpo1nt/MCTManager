package org.braekpo1nt.mctmanager.games.game.footrace;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ToString(callSuper = true)
@Getter
@Setter
public class FootRaceParticipant extends ParticipantData {
    
    /**
     * The lap cooldown for the participant crossing the finish line
     */
    private long lapCooldown;
    /**
     * The participant's current lap
     */
    private int lap;
    /**
     * The participant's current checkpoint
     */
    private int currentCheckpoint;
    /**
     * The participant's current checkpoint, but can
     * backtrack to help track if they're going the wrong way
     * without also allowing them to pass the finish line by
     * going backwards then forwards.
     */
    private int wrongWayCheckpoint;
    /**
     * Whether the participant finished the race or not
     */
    private boolean finished;
    /**
     * The participant's placement upon finishing the race.
     * 0 when they haven't finished.
     */
    private int placement;
    /**
     * The last time the "Wrong Way!" title was shown to the participant.
     */
    private long lastWrongWayTitleTime;
    private long wrongWayCounterStart = -1L;
    private long rightWayCounterStart = -1L;
    private boolean showingWrongWayAlert = false;
    private double lastDistToNext = Double.MAX_VALUE;
    
    /**
     * Used for rejoining, saved when quitting,
     * teleported to this pos when rejoining and set to null
     */
    private @Nullable Location lastPosition;
    
    public FootRaceParticipant(@NotNull Participant participant, int currentCheckpoint, int score) {
        super(participant, score);
        this.lapCooldown = System.currentTimeMillis();
        this.lap = 1;
        this.currentCheckpoint = currentCheckpoint;
        this.wrongWayCheckpoint = currentCheckpoint;
        this.finished = false;
        this.placement = 0;
        this.lastWrongWayTitleTime = 0L;
    }
    
    public FootRaceParticipant(Participant participant, QuitData quitData) {
        super(participant, quitData.getScore());
        this.lapCooldown = System.currentTimeMillis();
        this.lap = quitData.getLap();
        this.currentCheckpoint = quitData.getCurrentCheckpoint();
        this.wrongWayCheckpoint = quitData.getCurrentCheckpoint();
        this.finished = quitData.isFinished();
        this.placement = quitData.getPlacement();
        this.lastWrongWayTitleTime = quitData.getLastWrongWayTitleTime();
    }
    
    /**
     * Also sets the {@link #wrongWayCheckpoint}
     * @param currentCheckpoint the checkpoint index
     */
    public void setCurrentCheckpoint(int currentCheckpoint) {
//        Main.logf("%s reached %s", getName(), currentCheckpoint);
        this.currentCheckpoint = currentCheckpoint;
        this.wrongWayCheckpoint = currentCheckpoint;
    }
    
    public void setWrongWayCheckpoint(int wrongWayCheckpoint) {
//        Main.logf("%s reached wrong way %s", getName(), wrongWayCheckpoint);
        this.wrongWayCheckpoint = wrongWayCheckpoint;
    }
    
    /**
     * Holds data for the participant when they leave the game
     */
    @Data
    public static class QuitData implements QuitDataBase {
        
        private final int score;
        /**
         * The participant's current lap
         */
        private final int lap;
        /**
         * The participant's current checkpoint
         */
        private final int currentCheckpoint;
        /**
         * Whether the participant finished the race or not
         */
        private final boolean finished;
        /**
         * The participant's placement upon finishing the race
         */
        private final int placement;
        /**
         * The last time the "Wrong Way!" title was shown to the participant.
         */
        private final long lastWrongWayTitleTime;
    }
    
    // TODO: why isn't lastPosition stored? It should tp them back to where they were standing
    public QuitData getQuitData(@NotNull Location lastPosition) {
        return new QuitData(
                getScore(),
                lap,
                currentCheckpoint,
                finished,
                placement,
                lastWrongWayTitleTime
        );
    }
}
