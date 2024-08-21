package org.braekpo1nt.mctmanager.games.game.footrace.states;

import org.bukkit.entity.Player;

public interface FootRaceState {
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    void initializeParticipant(Player participant);
    void resetParticipant(Player participant);
    // listener handlers
    void onParticipantMove(Player participant);
}
