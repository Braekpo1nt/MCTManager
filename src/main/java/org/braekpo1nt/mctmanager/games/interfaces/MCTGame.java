package org.braekpo1nt.mctmanager.games.interfaces;

import org.braekpo1nt.mctmanager.games.enums.GameType;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * An MCT game. 
 */
public interface MCTGame {
    GameType getType();
    void start(List<Player> newParticipants);
    void stop();
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
}
