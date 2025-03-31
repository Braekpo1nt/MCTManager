package org.braekpo1nt.mctmanager.games.experimental;

import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Prevents participants from losing hunger. 
 * @param <P>
 */
public class PreventHungerLoss<P extends Participant> extends GameListener<P> {
    public PreventHungerLoss(@NotNull GameData<P> gameData) {
        super(gameData);
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        P participant = gameData.getParticipant(event.getEntity().getUniqueId());
        if (participant == null) {
            return;
        }
        participant.setFoodLevel(20);
        event.setCancelled(true);
    }
}
