package org.braekpo1nt.mctmanager.games.experimental;

import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public interface DamageListener<P extends ParticipantData> extends GameListener<P> {
    @EventHandler
    default void entityDamageEvent(EntityDamageEvent event) {
        P participant = getParticipant(event.getEntity().getUniqueId());
        if (participant == null) {
            
        }
    }
}
