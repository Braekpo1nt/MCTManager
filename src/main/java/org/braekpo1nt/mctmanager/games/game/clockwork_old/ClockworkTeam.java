package org.braekpo1nt.mctmanager.games.game.clockwork_old;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

class ClockworkTeam extends ScoredTeamData<ClockworkParticipant> {
    
    public ClockworkTeam(Team team, int score) {
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
