package org.braekpo1nt.mctmanager.games.game.spleef;

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
public class SpleefParticipant extends ParticipantData {
    
    private boolean alive;
    
    public SpleefParticipant(@NotNull Participant participant, int score, boolean alive) {
        super(participant, score);
        this.alive = alive;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
    }
}
