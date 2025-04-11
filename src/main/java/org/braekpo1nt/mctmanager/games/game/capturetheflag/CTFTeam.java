package org.braekpo1nt.mctmanager.games.game.capturetheflag;

import lombok.Data;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

public class CTFTeam extends ScoredTeamData<CTFParticipant> {
    public CTFTeam(Team team, int score) {
        super(team, score);
    }
    
    public CTFTeam(Team team, QuitData quitData) {
        super(team, quitData.getScore());
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore());
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
    }
}
