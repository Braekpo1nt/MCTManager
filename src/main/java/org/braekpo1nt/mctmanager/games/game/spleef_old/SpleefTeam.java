package org.braekpo1nt.mctmanager.games.game.spleef_old;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

public class SpleefTeam extends ScoredTeamData<SpleefParticipant> {
    
    public SpleefTeam(Team team, int score) {
        super(team, score);
    }
    
    @Data
    public static class QuitData {
        private final int score;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
}
