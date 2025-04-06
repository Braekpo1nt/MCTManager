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
    
    /**
     * @return true if at least one member is alive, false otherwise
     */
    public boolean isAlive() {
        return getParticipants().stream().anyMatch(ClockworkParticipant::isAlive);
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
    }
}
