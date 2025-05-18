package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.braekpo1nt.mctmanager.games.base.states.GameStateBase;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchTeam;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.jetbrains.annotations.NotNull;

public interface CaptureTheFlagMatchState extends GameStateBase<CTFMatchParticipant, CTFMatchTeam> {
    void nextState();
    
    // event handlers
    void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFMatchParticipant participant);
}
