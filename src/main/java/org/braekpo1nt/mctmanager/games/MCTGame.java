package org.braekpo1nt.mctmanager.games;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * An MCT game. 
 */
public interface MCTGame {
    void start(List<Player> participants);
    void stop();
}
