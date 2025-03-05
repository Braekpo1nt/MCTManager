package org.braekpo1nt.mctmanager.games.game.footrace.states;

import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceTeam;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;

public interface FootRaceState {
    void onParticipantJoin(Participant newParticipant, Team team);
    void onParticipantQuit(FootRaceParticipant participant, FootRaceTeam team);
    void initializeParticipant(Participant participant);
    void resetParticipant(FootRaceParticipant participant);
    // listener handlers
    void onParticipantMove(FootRaceParticipant participant);
}
