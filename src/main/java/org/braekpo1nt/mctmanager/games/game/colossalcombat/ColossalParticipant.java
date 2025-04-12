package org.braekpo1nt.mctmanager.games.game.colossalcombat;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

public class ColossalParticipant extends ParticipantData {
    public ColossalParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
    }
}
