package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.braekpo1nt.mctmanager.participant.Participant;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public interface CaptureTheFlagMatchState {
    void onParticipantJoin(Participant participant);
    void onParticipantQuit(Participant participant);
    void nextState();
    
    default void stop() {
        // do nothing
    }
    
    // event handlers
    void onPlayerDamage(EntityDamageEvent event);
    void onPlayerLoseHunger(FoodLevelChangeEvent event);
    void onPlayerMove(PlayerMoveEvent event);
    void onClickInventory(InventoryClickEvent event);
    void onPlayerDeath(PlayerDeathEvent event);
}
