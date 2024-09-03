package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public interface FarmRushState {
    
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    default void onParticipantDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
    
    
}
