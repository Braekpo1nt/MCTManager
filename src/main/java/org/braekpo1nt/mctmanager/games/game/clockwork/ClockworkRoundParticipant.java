package org.braekpo1nt.mctmanager.games.game.clockwork;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
public class ClockworkRoundParticipant extends ParticipantData {
    
    private boolean alive;
    
    public ClockworkRoundParticipant(@NotNull ClockworkParticipant participant, boolean alive) {
        super(participant, participant.getScore());
        this.alive = alive;
    }
    
    public ClockworkRoundParticipant(@NotNull ClockworkParticipant participant) {
        this(participant, true);
    }
    
}
