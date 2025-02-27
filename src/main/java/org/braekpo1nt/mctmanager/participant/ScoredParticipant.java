package org.braekpo1nt.mctmanager.participant;

import org.jetbrains.annotations.NotNull;

public class ScoredParticipant extends Participant {
    
    protected int score;
    
    public ScoredParticipant(@NotNull Participant participant, int score) {
        super(participant);
        this.score = score;
    }
    
    public ScoredParticipant(@NotNull Participant participant) {
        super(participant);
        this.score = 0;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    @Override
    public int getScore() {
        return this.score;
    }
}
