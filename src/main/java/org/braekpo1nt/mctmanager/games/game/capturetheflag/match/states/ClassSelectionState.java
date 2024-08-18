package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.ClassPicker;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClassSelectionState implements CaptureTheFlagMatchState {
    
    private final CaptureTheFlagMatch context;
    private final ClassPicker northClassPicker;
    private final ClassPicker southClassPicker;
    
    public ClassSelectionState(CaptureTheFlagMatch context) {
        this.context = context;
        this.northClassPicker = context.getNorthClassPicker();
        this.southClassPicker = context.getSouthClassPicker();
        northClassPicker.start(
                context.getPlugin(), 
                context.getNorthParticipants(), 
                context.getConfig().getLoadouts());
        southClassPicker.start(
                context.getPlugin(), 
                context.getSouthParticipants(), 
                context.getConfig().getLoadouts());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getClassSelectionDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .sidebarPrefix(Component.text("Class selection: "))
                .titleAudience(Audience.audience(context.getAllParticipants()))
                .onCompletion(() -> {
                    northClassPicker.stop(true);
                    southClassPicker.stop(true);
                    context.setState(new MatchActiveState(context));
                })
                .build());
    }
    
    @Override
    public void stop() {
        northClassPicker.stop(false);
        southClassPicker.stop(false);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        context.initializeParticipant(participant);
        context.getTopbar().linkToTeam(participant.getUniqueId(), teamId);
        if (context.getMatchPairing().northTeam().equals(teamId)) {
            northClassPicker.addTeamMate(participant);
        } else {
            southClassPicker.addTeamMate(participant);
        }
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
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
