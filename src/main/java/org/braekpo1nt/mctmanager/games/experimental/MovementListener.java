package org.braekpo1nt.mctmanager.games.experimental;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public interface MovementListener<P> extends GameListener<P> {
    @EventHandler
    default void playerMoveEvent(PlayerMoveEvent event) {
        P participant = getParticipant(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        playerMoveEvent(event, participant);
    }
    
    void playerMoveEvent(PlayerMoveEvent event, P participant);
}
