package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameOverState implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    
    public GameOverState(@NotNull FarmRushGame context) {
        this.context = context;
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
}
