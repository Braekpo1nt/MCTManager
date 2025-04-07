package org.braekpo1nt.mctmanager.games.game.clockwork_old;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

class ClockworkParticipant extends ParticipantData {
    public ClockworkParticipant(@NotNull Participant participant, int score) {
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
