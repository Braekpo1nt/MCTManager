package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.jetbrains.annotations.NotNull;

public class RoundActiveState extends ClockworkStateBase {
    
    public RoundActiveState(@NotNull ClockworkGame context) {
        super(context);
        context.getChaosManager().start();
    }
    
    @Override
    public void cleanup() {
        context.getChaosManager().stop();
    }
    
    private void stop() {
        cleanup();
        context.setState(new RoundOverState(context));
    }
    
}
