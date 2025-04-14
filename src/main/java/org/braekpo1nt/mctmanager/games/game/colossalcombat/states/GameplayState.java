package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.config.ColossalCombatConfig;
import org.jetbrains.annotations.NotNull;

public abstract class GameplayState extends ColossalCombatStateBase {
    
    protected final @NotNull ColossalCombatConfig config;
    
    public GameplayState(@NotNull ColossalCombatGame context) {
        super(context);
        this.config = context.getConfig();
    }
    
    
}
