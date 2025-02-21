package org.braekpo1nt.mctmanager.games.game.clockwork;

import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;

public class ClockworkTeam extends TeamData<ClockworkParticipant> {
    
    public ClockworkTeam(Team team) {
        super(team);
    }
    
    public boolean isAlive() {
        return getParticipants().stream().anyMatch(ClockworkParticipant::isAlive);
    }
    
    public int getAlive() {
        return (int) getParticipants().stream().filter(ClockworkParticipant::isAlive).count();
    }
}
