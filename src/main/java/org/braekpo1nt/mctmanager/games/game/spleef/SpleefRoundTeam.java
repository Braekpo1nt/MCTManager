package org.braekpo1nt.mctmanager.games.game.spleef;

import org.braekpo1nt.mctmanager.participant.ScoredTeamData;

public class SpleefRoundTeam extends ScoredTeamData<SpleefRoundParticipant> {
    
    public SpleefRoundTeam(SpleefTeam team) {
        super(team, team.getScore());
    }
    
}
