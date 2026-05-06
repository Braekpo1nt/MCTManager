package org.braekpo1nt.mctmanager.games.game.example;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class ExampleParticipant extends ParticipantData {
    
    private boolean gliding;
    private boolean alive;
    
    public ExampleParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
        this.gliding = false;
        this.alive = true;
    }
    
    public ExampleParticipant(@NotNull Participant participant, @NotNull QuitData quitData) {
        super(participant, quitData.getScore());
        this.gliding = false;
        this.alive = quitData.isAlive();
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore(), alive);
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
        private final boolean alive;
    }
}
