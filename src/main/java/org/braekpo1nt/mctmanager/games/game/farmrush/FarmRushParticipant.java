package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

public class FarmRushParticipant extends ParticipantData {
    public FarmRushParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
    }
    
    @Data
    public static class QuitData {
        private final int score;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
}
