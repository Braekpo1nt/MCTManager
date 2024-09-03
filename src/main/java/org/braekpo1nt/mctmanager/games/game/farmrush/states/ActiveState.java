package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class ActiveState implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    
    public ActiveState(@NotNull FarmRushGame context) {
        this.context = context;
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        
    }
}
