package org.braekpo1nt.mctmanager.games.game.clockwork;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

public class ClockworkTeam extends ScoredTeamData<ClockworkParticipant> {
    public ClockworkTeam(Team team, int score) {
        super(team, score);
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    @Data
    public class QuitData implements QuitDataBase {
        private final int score;
    }
}
