package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import net.kyori.adventure.audience.Audience;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class MatchOverState implements CaptureTheFlagMatchState {
    
    private final CaptureTheFlagMatch context;
    
    public MatchOverState(CaptureTheFlagMatch context) {
        this.context = context;
        Audience.audience(context.getAllParticipants()).showTitle(UIUtils.matchOverTitle());
        for (Player participant : context.getAllParticipants()) {
            if (context.getParticipantsAreAlive().get(participant.getUniqueId())) {
                participant.getInventory().clear();
                participant.closeInventory();
                ParticipantInitializer.resetHealthAndHunger(participant);
                ParticipantInitializer.clearStatusEffects(participant);
            }
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
        participant.getInventory().clear();
        participant.closeInventory();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event) {
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        
    }
}
