package org.braekpo1nt.mctmanager.games.game.colossalcombat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.games.experimental.Affiliated;
import org.braekpo1nt.mctmanager.games.experimental.Affiliation;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class ColossalParticipant extends ParticipantData implements Affiliated {
    
    private final @NotNull Affiliation affiliation;
    private boolean alive;
    
    public ColossalParticipant(@NotNull Participant participant, int score, @NotNull Affiliation affiliation) {
        super(participant, score);
        this.affiliation = affiliation;
        this.alive = true;
    }
    
    public ColossalParticipant(@NotNull Participant participant, @NotNull QuitData quitData) {
        this(participant, quitData.getScore(), quitData.getAffiliation());
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore(), this.affiliation);
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
        private final @NotNull Affiliation affiliation;
    }
}
