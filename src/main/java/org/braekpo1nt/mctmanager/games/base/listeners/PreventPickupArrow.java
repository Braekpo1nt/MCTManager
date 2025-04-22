package org.braekpo1nt.mctmanager.games.base.listeners;

import org.braekpo1nt.mctmanager.games.base.GameBase;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Prevents participants from picking up arrows
 * @param <P> the type of participant to prevent
 */
public class PreventPickupArrow<P extends ParticipantData> extends GameListener<P> {
    
    public PreventPickupArrow(@NotNull GameBase<P, ?, ?, ?, ?> context) {
        super(context);
    }
    
    @EventHandler
    public void onPlayerPickupArrow(PlayerPickupArrowEvent event) {
        P participant = context.getParticipant(event.getPlayer().getUniqueId());
        if (participant == null) {
            return;
        }
        event.getArrow().remove();
        event.setCancelled(true);
    }
}
