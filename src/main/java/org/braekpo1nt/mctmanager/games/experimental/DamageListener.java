package org.braekpo1nt.mctmanager.games.experimental;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public interface DamageListener<P> extends GameListener<P> {
    @EventHandler
    default void entityDamageEvent(EntityDamageEvent event) {
        P participant = getParticipant(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        entityDamageEvent(event, participant);
    }
    
    void entityDamageEvent(EntityDamageEvent event, P participant);
}
