package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class MatchActiveState implements CaptureTheFlagMatchState {
    
    private final CaptureTheFlagMatch context;
    
    public MatchActiveState(CaptureTheFlagMatch context) {
        this.context = context;
        for (Player participant : context.getAllParticipants()) {
            participant.closeInventory();
        }
        context.messageAllParticipants(Component.text("Begin!"));
        context.openGlassBarriers();
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundTimerDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .sidebarPrefix(Component.text("Starting: "))
                .onCompletion(() -> {
                    onBothTeamsLose(Component.text("Time ran out."));
                })
                .build());
    }
    
    private void onBothTeamsLose(Component reason) {
        context.messageAllParticipants(Component.empty()
                .append(Component.text("Game over. "))
                .append(reason));
        context.setState(new MatchOverState(context));
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
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        
    }
}
