package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFTeam;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

public interface CaptureTheFlagState extends GameStateBase<CTFParticipant, CTFTeam> {
    
    // event handlers
    void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFParticipant participant);
    
    default void messageOnDeckParticipants(@NotNull Component message) {
        // do nothing
    }
    
    default @NotNull Audience getOnDeckParticipants() {
        // do nothing
        return Audience.empty();
    }
}
