package org.braekpo1nt.mctmanager.games.game.footrace;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

public class FootRaceTeam extends ScoredTeamData<FootRaceParticipant> {
    /**
     * The lowest lap number across this team's participants
     */
    @Getter @Setter private int minimumLap;
    
    public FootRaceTeam(Team team, int score) {
        super(team, score);
        this.minimumLap = 1;
    }
    
    public FootRaceTeam(Team team, FootRaceTeam.QuitData quitData) {
        super(team, quitData.getScore());
        this.minimumLap = quitData.getMinimumLap();
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
        private final int minimumLap;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore(), minimumLap);
    }
}
