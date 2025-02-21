package org.braekpo1nt.mctmanager.games.game.spleef;

import lombok.*;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
public class SpleefParticipant extends Participant {
    
    private boolean alive;
    
    public SpleefParticipant(@NotNull Participant participant) {
        super(participant);
        alive = true;
    }
    
    public SpleefParticipant(@NotNull Participant participant, SpleefQuitData quitData) {
        super(participant);
        this.alive = quitData.isAlive();
    }
    
    public SpleefQuitData getQuitData() {
        return new SpleefQuitData(alive);
    }
}
