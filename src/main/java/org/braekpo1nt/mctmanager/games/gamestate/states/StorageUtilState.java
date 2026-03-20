package org.braekpo1nt.mctmanager.games.gamestate.states;

import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.jetbrains.annotations.NotNull;

public abstract class StorageUtilState {
    
    protected final @NotNull GameStateStorageUtil context;
    protected final @NotNull GameStateService gameStateService;
    
    public StorageUtilState(@NotNull GameStateStorageUtil context) {
        this.context = context;
        this.gameStateService = context.getGameStateService();
    }
    
    public abstract void enter();
    
    public abstract void exit();
    
}
