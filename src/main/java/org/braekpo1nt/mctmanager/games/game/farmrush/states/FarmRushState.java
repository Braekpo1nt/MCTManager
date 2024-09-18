package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public interface FarmRushState {
    
    void onParticipantJoin(Player participant);
    void onParticipantQuit(FarmRushGame.Participant participant);
    default void onParticipantDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
    
    
    default void onCloseInventory(InventoryCloseEvent event, FarmRushGame.Participant participant) {
        // do nothing
    }
}
