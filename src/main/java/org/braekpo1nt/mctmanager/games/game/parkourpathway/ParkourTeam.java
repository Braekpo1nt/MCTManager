package org.braekpo1nt.mctmanager.games.game.parkourpathway;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.braekpo1nt.mctmanager.participant.QuitDataBase;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.braekpo1nt.mctmanager.participant.Team;

public class ParkourTeam extends ScoredTeamData<ParkourParticipant> {
    @Getter
    @Setter
    private int minSection;
    
    public ParkourTeam(Team team, int score) {
        super(team, score);
        this.minSection = 0;
    }
    
    public QuitData getQuitData() {
        return new QuitData(getScore(), getMinSection());
    }
    
    @Data
    public static class QuitData implements QuitDataBase {
        private final int score;
        private final int minSection;
        
    }
}
