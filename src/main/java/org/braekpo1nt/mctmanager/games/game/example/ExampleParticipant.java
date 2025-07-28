package org.braekpo1nt.mctmanager.games.game.example;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

public class ExampleParticipant extends ParticipantData {
    
    @Getter
    @Setter
    private boolean gliding;
    
    public ExampleParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
        this.gliding = false;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
    }
}
