package org.braekpo1nt.mctmanager.games.game.spleef;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
public class SpleefRoundParticipant extends ParticipantData {
    
    private boolean alive;
    
    public SpleefRoundParticipant(@NotNull SpleefParticipant participant) {
        super(participant, participant.getScore());
        alive = true;
    }
    
    public SpleefRoundParticipant(@NotNull SpleefParticipant participant, QuitData quitData) {
        super(participant, participant.getScore());
        alive = quitData.isAlive();
    }
    
    @Data
    public static class QuitData {
        private final boolean alive;
    }
    
    public QuitData getQuitData() {
        return new QuitData(alive);
    }
}
