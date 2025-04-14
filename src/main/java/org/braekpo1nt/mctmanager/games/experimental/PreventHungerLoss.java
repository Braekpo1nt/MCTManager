package org.braekpo1nt.mctmanager.games.experimental;

import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Prevents participants from losing hunger. 
 * @param <P>
 */
public class PreventHungerLoss<P extends ParticipantData> extends GameListener<P> {
    
    public PreventHungerLoss(@NotNull GameBase<P, ?, ?, ?, ?> context) {
        super(context);
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        P participant = context.getParticipant(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
}
