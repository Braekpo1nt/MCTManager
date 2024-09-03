package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.jetbrains.annotations.NotNull;

public class ActiveState implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    
    public ActiveState(@NotNull FarmRushGame context) {
        this.context = context;
    }
    
}
