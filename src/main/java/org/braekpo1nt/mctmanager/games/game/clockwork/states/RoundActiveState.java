package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.jetbrains.annotations.NotNull;

public class RoundActiveState extends ClockworkStateBase {
    public RoundActiveState(@NotNull ClockworkGame context) {
        super(context);
        
    }
    
    private void stop() {
        context.setState(new RoundOverState(context));
    }
}
