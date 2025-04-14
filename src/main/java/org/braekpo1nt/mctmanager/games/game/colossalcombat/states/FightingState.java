package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class FightingState extends GameplayState {
    public FightingState(@NotNull ColossalCombatGame context) {
        super(context);
        context.getTimerManager().start(Timer.builder()
                .duration(config.getCaptureTheFlagDuration())
                        .withTopbar(context.getTopbar())
                        .withSidebar(context.getAdminSidebar(), "timer")
                        .onCompletion(() -> context.setState(new CTFState(context)))
                .build());
    }
    
    
}
