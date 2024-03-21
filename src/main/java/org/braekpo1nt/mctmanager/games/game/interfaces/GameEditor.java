package org.braekpo1nt.mctmanager.games.game.interfaces;

import org.bukkit.entity.Player;

import java.util.List;

public interface GameEditor extends Configurable {
    void start(List<Player> newParticipants);
    void stop();
}
