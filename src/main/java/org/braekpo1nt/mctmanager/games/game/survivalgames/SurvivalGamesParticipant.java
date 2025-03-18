package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.jetbrains.annotations.NotNull;

@ToString(callSuper = true)
@Getter
@Setter
public class SurvivalGamesParticipant extends ParticipantData {
    
    private boolean alive;
    private int kills;
    private int deaths;
    
    public SurvivalGamesParticipant(@NotNull Participant participant, int score) {
        super(participant, score);
        this.alive = true;
        this.kills = 0;
        this.deaths = 0;
    }
    
    public SurvivalGamesParticipant(@NotNull Participant participant, @NotNull SurvivalGamesParticipant.QuitData quitData) {
        super(participant, quitData.getScore());
        this.alive = quitData.isAlive();
        this.kills = quitData.getKills();
        this.deaths = quitData.getDeaths();
    }
    
    public QuitData getQuitData() {
        return new QuitData(
                getScore(),
                alive,
                kills,
                deaths
        );
    }
    
    @Data
    public static class QuitData {
        private final int score;
        private final boolean alive;
        private final int kills;
        private final int deaths;
    }
}
