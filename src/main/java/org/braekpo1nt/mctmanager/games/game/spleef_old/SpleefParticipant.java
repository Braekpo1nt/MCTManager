package org.braekpo1nt.mctmanager.games.game.spleef_old;

import lombok.*;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
public class SpleefParticipant extends ParticipantData {
    
    public SpleefParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
    }
    
    public SpleefParticipant(@NotNull Participant participant, QuitData quitData) {
        super(participant, quitData.getScore());
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    @Data
    public static class QuitData {
        private final int score;
    }
}
