package org.braekpo1nt.mctmanager.games.game.clockwork;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
public class ClockworkParticipant extends Participant {
    
    private boolean alive;
    
    public ClockworkParticipant(@NotNull Participant participant, boolean alive) {
        super(participant);
        this.alive = alive;
    }
    
    public ClockworkParticipant(@NotNull Participant participant, @NotNull ClockworkQuitData quitData) {
        this(participant, quitData.isAlive());
    }
    
    public ClockworkParticipant(@NotNull Participant participant) {
        this(participant, true);
    }
    
    public ClockworkQuitData getQuitData() {
        return new ClockworkQuitData(alive);
    }
    
}
