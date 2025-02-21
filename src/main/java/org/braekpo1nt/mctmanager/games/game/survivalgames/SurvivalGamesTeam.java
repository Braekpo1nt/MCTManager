package org.braekpo1nt.mctmanager.games.game.survivalgames;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
@Setter
public class SurvivalGamesTeam extends TeamData<SurvivalGamesParticipant> {
    
    public SurvivalGamesTeam(Team team) {
        super(team);
    }
    
    /**
     * @return True if at least one participant on this team is alive, false otherwise
     */
    public boolean isAlive() {
        return getParticipants().stream().anyMatch(SurvivalGamesParticipant::isAlive);
    }
}
