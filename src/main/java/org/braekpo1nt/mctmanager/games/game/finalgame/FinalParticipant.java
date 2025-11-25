package org.braekpo1nt.mctmanager.games.game.finalgame;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.games.base.Affiliated;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class FinalParticipant extends ParticipantData implements Affiliated {
    private final @NotNull Affiliation affiliation;
    private boolean alive;
    private int kills;
    private int deaths;
    
    public FinalParticipant(@NotNull Participant participant, @NotNull Affiliation affiliation, boolean alive, int kills, int deaths, int score) {
        super(participant, score);
        this.affiliation = affiliation;
        this.alive = alive;
        this.kills = kills;
        this.deaths = deaths;
    }
    
    public FinalParticipant(@NotNull Participant participant, boolean alive, @NotNull QuitData quitData) {
        this(
                participant,
                quitData.getAffiliation(),
                alive,
                quitData.getKills(),
                quitData.getDeaths(),
                quitData.getScore()
        );
    }
    
    public QuitData getQuitData() {
        return new QuitData(
                this.affiliation,
                this.kills,
                this.deaths,
                getScore()
        );
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final @NotNull Affiliation affiliation;
        private final int kills;
        private final int deaths;
        private final int score;
    }
    
}
