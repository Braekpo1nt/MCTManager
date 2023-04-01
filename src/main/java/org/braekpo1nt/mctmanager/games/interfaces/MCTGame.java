package org.braekpo1nt.mctmanager.games.interfaces;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * An MCT game. 
 */
public interface MCTGame {
    void start(List<Player> participants);
    void stop();
    void onPlayerJoin(Player player);
    void onPlayerQuit(Player player);
}
