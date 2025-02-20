package org.braekpo1nt.mctmanager.games.game.footrace.states;

import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;

public interface FootRaceState {
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(FootRaceParticipant participant);
    void initializeParticipant(Participant participant);
    void resetParticipant(FootRaceParticipant participant);
    // listener handlers
    void onParticipantMove(FootRaceParticipant participant);
}
