package org.braekpo1nt.mctmanager.games.game.clockwork;

import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.participant.TeamData;

@ToString(callSuper = true)
public class ClockworkTeam extends TeamData<ClockworkParticipant> {
    
    public ClockworkTeam(Team team) {
        super(team);
    }
    
    /**
     * @return true if at least one member is alive, false otherwise
     */
    public boolean isAlive() {
        return getParticipants().stream().anyMatch(ClockworkParticipant::isAlive);
    }
    
    /**
     * @return true if no members are alive, false otherwise
     */
    public boolean isDead() {
        for (ClockworkParticipant participant : getParticipants()) {
            if (participant.isAlive()) {
                return false;
            }
        }
        return true;
//        return getParticipants().stream().noneMatch(ClockworkParticipant::isAlive);
    }
    
    public int getAlive() {
        return (int) getParticipants().stream().filter(ClockworkParticipant::isAlive).count();
    }
}
