package org.braekpo1nt.mctmanager.games.game.footrace;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

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
     * Whether the participant finished the race or not
     */
    private boolean finished;
    /**
     * The participant's placement upon finishing the race.
     * 0 when they haven't finished.
     */
    private int placement;
    
    public FootRaceParticipant(@NotNull Participant participant, int currentCheckpoint, int score) {
        super(participant, score);
        this.lapCooldown = System.currentTimeMillis();
        this.lap = 1;
        this.currentCheckpoint = currentCheckpoint;
        this.finished = false;
        this.placement = 0;
    }
    
    public FootRaceParticipant(Participant participant, QuitData quitData) {
        super(participant, quitData.getScore());
        this.lapCooldown = System.currentTimeMillis();
        this.lap = quitData.getLap();
        this.currentCheckpoint = quitData.getCurrentCheckpoint();
        this.finished = quitData.isFinished();
        this.placement = quitData.getPlacement();
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
    }
    
    public QuitData getQuitData() {
        return new QuitData(
                getScore(),
                lap,
                currentCheckpoint,
                finished,
                placement
        );
    }
}
