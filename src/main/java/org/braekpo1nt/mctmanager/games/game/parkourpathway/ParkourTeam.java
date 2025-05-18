package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

public class ParkourTeam extends ScoredTeamData<ParkourParticipant> {
    public ParkourTeam(Team team, int score) {
        super(team, score);
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
    
    }
}
