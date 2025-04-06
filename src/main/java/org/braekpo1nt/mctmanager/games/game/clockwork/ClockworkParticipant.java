package org.braekpo1nt.mctmanager.games.game.clockwork;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
public class ClockworkParticipant extends ParticipantData {
    
    private boolean alive;
    
    public ClockworkParticipant(@NotNull Participant participant, int score, boolean alive) {
        super(participant, score);
        this.alive = alive;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    @Data
    public class QuitData implements QuitDataBase {
        private final int score;
    }
}
