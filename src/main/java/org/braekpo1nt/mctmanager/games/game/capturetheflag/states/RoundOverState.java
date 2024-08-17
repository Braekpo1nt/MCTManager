package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class RoundOverState implements CaptureTheFlagState {
    
    private final CaptureTheFlagGame context;
    
    public RoundOverState(CaptureTheFlagGame context) {
        this.context = context;
        if (context.getRoundManager().hasNextRound()) {
            context.getRoundManager().nextRound();
            context.setState(new PreRoundState(context));
        } else {
            // TODO: game over state
        }
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        
    }
    
    @Override
    public void resetParticipant(Player participant) {
        
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        
    }
}
