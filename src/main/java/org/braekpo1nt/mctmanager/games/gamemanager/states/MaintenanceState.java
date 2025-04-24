package org.braekpo1nt.mctmanager.games.gamemanager.states;

import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

public class MaintenanceState extends GameManagerState {
    
    
    public MaintenanceState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference) {
        super(context, contextReference);
    }
}
