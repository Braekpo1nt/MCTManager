package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public interface CaptureTheFlagMatchState {
    void onParticipantJoin(Player participant);
    void onParticipantQuit(Player participant);
    void initializeParticipant(Player participant);
    void resetParticipant(Player participant);
    
    default void cancelAllTasks() {
        // do nothing
    }
    
    // event handlers
    void onPlayerDamage(EntityDamageEvent event);
    void onPlayerLoseHunger(FoodLevelChangeEvent event);
    void onPlayerMove(PlayerMoveEvent event);
    void onClickInventory(InventoryClickEvent event);
    void onPlayerDeath(PlayerDeathEvent event);
}
