package org.braekpo1nt.mctmanager.games.game.example;

import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

public class ExampleParticipant extends ParticipantData {
    public ExampleParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    public static class QuitData extends QuitDataBase {
        public QuitData(int score) {
            super(score);
        }
    }
}
