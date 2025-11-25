package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KitSelectionState extends FinalStateBase {
    
    private @Nullable Timer timer;
    
    public KitSelectionState(@NotNull FinalGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}
