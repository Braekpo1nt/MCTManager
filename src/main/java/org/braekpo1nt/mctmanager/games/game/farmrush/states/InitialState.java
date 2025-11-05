package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import org.braekpo1nt.mctmanager.games.base.states.DoNothingState;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushParticipant;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushTeam;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InitialState implements FarmRushState, DoNothingState<FarmRushParticipant, FarmRushTeam> {
    @Override
    public void onParticipantCloseInventory(InventoryCloseEvent event, FarmRushParticipant participant) {
        
    }
    
    @Override
    public void onParticipantPlaceBlock(BlockPlaceEvent event, FarmRushParticipant participant) {
        
    }
    
    @Override
    public void onParticipantOpenInventory(InventoryOpenEvent event, FarmRushParticipant participant) {
        
    }
    
    @Override
    public void showMaterialGui(FarmRushParticipant participant) {
        
    }
}
