package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public class SurvivalGamesParticipant extends Participant {
    
    private boolean alive;
    private int kills;
    private int deaths;
    
    public SurvivalGamesParticipant(@NotNull Participant participant) {
        super(participant);
        this.alive = true;
        this.kills = 0;
        this.deaths = 0;
    }
    
    public SurvivalGamesParticipant(@NotNull Participant participant, @NotNull SurvivalGamesQuitData quitData) {
        super(participant);
        this.alive = quitData.isAlive();
        this.kills = quitData.getKills();
        this.deaths = quitData.getDeaths();
    }
    
    public SurvivalGamesQuitData getQuitData() {
        return new SurvivalGamesQuitData(
                alive,
                kills,
                deaths
        );
    }
}
