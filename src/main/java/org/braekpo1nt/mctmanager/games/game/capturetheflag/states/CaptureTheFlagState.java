package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFTeam;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public interface CaptureTheFlagState extends GameStateBase<CTFParticipant, CTFTeam> {
    
    // event handlers
    void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFParticipant participant);
    void onParticipantClickInventory(@NotNull InventoryClickEvent event, @NotNull CTFParticipant participant);
    
}
