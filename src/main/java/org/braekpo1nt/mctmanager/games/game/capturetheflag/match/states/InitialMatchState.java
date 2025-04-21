package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.braekpo1nt.mctmanager.games.base.states.DoNothingState;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchTeam;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class InitialMatchState implements CaptureTheFlagMatchState, DoNothingState<CTFMatchParticipant, CTFMatchTeam> {
    @Override
    public void nextState() {
        
    }
    
    @Override
    public void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFMatchParticipant participant) {
        
    }
    
    @Override
    public void onParticipantClickInventory(@NotNull InventoryClickEvent event, @NotNull CTFMatchParticipant participant) {
        
    }
}
