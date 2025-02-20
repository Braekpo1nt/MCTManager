package org.braekpo1nt.mctmanager.games.game.footrace;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public class FootRaceParticipant extends Participant {
    
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
    
    public FootRaceParticipant(@NotNull Participant participant, int currentCheckpoint) {
        super(participant);
        this.lapCooldown = System.currentTimeMillis();
        this.lap = 1;
        this.currentCheckpoint = currentCheckpoint;
        this.finished = false;
        this.placement = 0;
    }
    
    public FootRaceParticipant(Participant participant, QuitParticipant quitData) {
        super(participant);
        this.lapCooldown = System.currentTimeMillis();
        this.lap = quitData.getLap();
        this.currentCheckpoint = quitData.getCurrentCheckpoint();
        this.finished = quitData.isFinished();
        this.placement = quitData.getPlacement();
    }
    
    public QuitParticipant getQuitData() {
        return new QuitParticipant(
                lap,
                currentCheckpoint,
                finished,
                placement
        );
    }
}
