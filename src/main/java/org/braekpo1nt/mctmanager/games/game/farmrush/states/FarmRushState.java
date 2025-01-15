package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public interface FarmRushState {
    
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(Participant participant);
    default void onParticipantDamage(EntityDamageEvent event) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "FarmRushState.onPlayerDamage()");
        event.setCancelled(true);
    }
    
    default void onCloseInventory(InventoryCloseEvent event, Participant participant) {
        // do nothing
    }
    
    default void onPlayerMove(PlayerMoveEvent event, Participant participant) {
        // do nothing
    }
    
    default void onPlaceBlock(BlockPlaceEvent event, Participant participant) {
        // do nothing
    }
    
    default void onPlayerOpenInventory(InventoryOpenEvent event) {
        // do nothing
    }
}
