package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public interface CaptureTheFlagState {
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(Participant participant);
    default void stop() {
        // do nothing
    }
    
    // event handlers
    void onPlayerDamage(EntityDamageEvent event);
    void onPlayerLoseHunger(FoodLevelChangeEvent event);
    void onClickInventory(InventoryClickEvent event);
    default void onPlayerDeath(PlayerDeathEvent event) {
        // do nothing
    }
    
    void onPlayerMove(PlayerMoveEvent event);
}
