package org.braekpo1nt.mctmanager.games.game.footrace.states;

import org.braekpo1nt.mctmanager.participant.Participant;

public interface FootRaceState {
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(Participant participant);
    void initializeParticipant(Participant participant);
    void resetParticipant(Participant participant);
    // listener handlers
    void onParticipantMove(Participant participant);
}
