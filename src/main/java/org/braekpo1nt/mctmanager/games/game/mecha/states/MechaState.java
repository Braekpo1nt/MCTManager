package org.braekpo1nt.mctmanager.games.game.mecha.states;

import org.bukkit.entity.Player;

public interface MechaState {
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    void initializeParticipant(Player participant);
    void resetParticipant(Player participant);
}
