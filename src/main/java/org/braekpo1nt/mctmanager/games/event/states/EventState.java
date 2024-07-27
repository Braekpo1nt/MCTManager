package org.braekpo1nt.mctmanager.games.event.states;

import org.bukkit.entity.Player;

public interface EventState {
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
}
