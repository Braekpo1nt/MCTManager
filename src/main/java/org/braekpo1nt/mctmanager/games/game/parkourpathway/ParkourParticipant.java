package org.braekpo1nt.mctmanager.games.game.parkourpathway;

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
public class ParkourParticipant extends Participant {
    
    /**
     * True if the participant has finished the last puzzle
     */
    private boolean finished;
    /**
     * The index of the puzzle the participant is solving
     */
    private int currentPuzzle;
    /**
     * The index of the checkpoint that the participant is on for their 
     * current puzzle (since each puzzle may have multiple checkpoints)
     */
    private int currentPuzzleCheckpoint;
    
    public ParkourParticipant(@NotNull ParkourParticipant participant) {
        super(participant);
    }
    
}
