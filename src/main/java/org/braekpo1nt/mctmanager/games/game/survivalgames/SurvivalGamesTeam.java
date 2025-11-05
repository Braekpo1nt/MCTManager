package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

@ToString(callSuper = true)
@Getter
@Setter
public class SurvivalGamesTeam extends ScoredTeamData<SurvivalGamesParticipant> {
    
    public SurvivalGamesTeam(Team team, int score) {
        super(team, score);
    }
    
    /**
     * @return True if at least one participant on this team is alive, false otherwise
     */
    public boolean isAlive() {
        return getParticipants().stream().anyMatch(SurvivalGamesParticipant::isAlive);
    }
    
    /**
     * @return true if at least one participant of this team is respawning, false otherwise
     */
    public boolean isRespawning() {
        return getParticipants().stream().anyMatch(SurvivalGamesParticipant::isRespawning);
    }
    
    public int getAlive() {
        return (int) getParticipants().stream().filter(SurvivalGamesParticipant::isAlive).count();
    }
    
    public int getDead() {
        return (int) getParticipants().stream().filter(p -> !p.isAlive()).count();
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
}
