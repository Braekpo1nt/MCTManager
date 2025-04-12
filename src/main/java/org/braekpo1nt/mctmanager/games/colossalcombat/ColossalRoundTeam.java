package org.braekpo1nt.mctmanager.games.colossalcombat;

import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;

class ColossalRoundTeam extends TeamData<ColossalRoundParticipant> {
    public ColossalRoundTeam(Team team) {
        super(team);
    }
    
    public int getAlive() {
        return ((int) getParticipants().stream().filter(ColossalRoundParticipant::isAlive).count());
    }
    
    public boolean isDead() {
        return getParticipants().stream().noneMatch(ColossalRoundParticipant::isAlive);
    }
}
