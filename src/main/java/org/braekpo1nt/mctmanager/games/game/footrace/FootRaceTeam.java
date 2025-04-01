package org.braekpo1nt.mctmanager.games.game.footrace;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

public class FootRaceTeam extends ScoredTeamData<FootRaceParticipant> {
    public FootRaceTeam(Team team, int score) {
        super(team, score);
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
}
