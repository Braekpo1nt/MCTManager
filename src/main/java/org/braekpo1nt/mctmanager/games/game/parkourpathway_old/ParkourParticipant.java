package org.braekpo1nt.mctmanager.games.game.parkourpathway_old;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
class ParkourParticipant extends ParticipantData {
    
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
    
    public ParkourParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
        this.finished = false;
        this.currentPuzzle = 0;
        this.currentPuzzleCheckpoint = 0;
    }
    
    public ParkourParticipant(@NotNull Participant participant, @NotNull ParkourParticipant.QuitData quitData) {
        super(participant, quitData.getScore());
        this.finished = quitData.isFinished();
        this.currentPuzzle = quitData.getCurrentPuzzle();
        this.currentPuzzleCheckpoint = quitData.getCurrentPuzzleCheckpoint();
    }
    
    public QuitData getQuitData(int unusedSkips) {
        return new QuitData(
                getScore(),
                finished,
                currentPuzzle,
                currentPuzzleCheckpoint,
                unusedSkips
        );
    }
    
    @Data
    public static class QuitData {
        private final int score;
        private final boolean finished;
        private final int currentPuzzle;
        private final int currentPuzzleCheckpoint;
        private final int numOfSkips;
    }
}
