package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
public class CTFParticipant extends ParticipantData {
    
    private int kills;
    private int deaths;
    
    public CTFParticipant(@NotNull Participant participant, int kills, int deaths, int score) {
        super(participant, score);
        this.kills = kills;
        this.deaths = deaths;
    }
    
    public CTFParticipant(@NotNull Participant participant) {
        this(participant, 0, 0, 0);
    }
    
    public QuitData getQuitData() {
        return new QuitData(kills, deaths, getScore());
    }
    
    @Data
    public static class QuitData {
        private final int kills;
        private final int deaths;
        private final int score;
    }
}
