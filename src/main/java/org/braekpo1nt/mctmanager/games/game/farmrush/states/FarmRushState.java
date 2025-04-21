package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushParticipant;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushTeam;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public interface FarmRushState extends GameStateBase<FarmRushParticipant, FarmRushTeam> {
    
    
    void onParticipantCloseInventory(InventoryCloseEvent event, FarmRushParticipant participant);
    
    void onParticipantPlaceBlock(BlockPlaceEvent event, FarmRushParticipant participant);
    
    void onParticipantOpenInventory(InventoryOpenEvent event, FarmRushParticipant participant);
}
