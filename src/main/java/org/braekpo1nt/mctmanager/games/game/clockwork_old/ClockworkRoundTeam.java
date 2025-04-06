package org.braekpo1nt.mctmanager.games.game.clockwork_old;

import lombok.ToString;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;

@ToString(callSuper = true)
class ClockworkRoundTeam extends ScoredTeamData<ClockworkRoundParticipant> {
    
    public ClockworkRoundTeam(ClockworkTeam team) {
        super(team, team.getScore());
    }
    
    /**
     * @return true if at least one member is alive, false otherwise
     */
    public boolean isAlive() {
        return getParticipants().stream().anyMatch(ClockworkRoundParticipant::isAlive);
    }
    
    /**
     * @return true if no members are alive, false otherwise
     */
    public boolean isDead() {
        for (ClockworkRoundParticipant participant : getParticipants()) {
            if (participant.isAlive()) {
                return false;
            }
        }
        return true;
//        return getParticipants().stream().noneMatch(ClockworkParticipant::isAlive);
    }
    
    public int getAlive() {
        return (int) getParticipants().stream().filter(ClockworkRoundParticipant::isAlive).count();
    }
}
