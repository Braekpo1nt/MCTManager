package org.braekpo1nt.mctmanager.games.game.capturetheflag.match.states;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CTFMatchTeam;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.match.CaptureTheFlagMatch;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class CaptureTheFlagMatchStateBase implements CaptureTheFlagMatchState{
    
    protected final @NotNull CaptureTheFlagMatch context;
    
    public CaptureTheFlagMatchStateBase(@NotNull CaptureTheFlagMatch context) {
        this.context = context;
    }
    
    @Override
    public void nextState() {
        
    }
    
    @Override
    public void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFMatchParticipant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantClickInventory(@NotNull InventoryClickEvent event, @NotNull CTFMatchParticipant participant) {
        // do nothing
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(CTFMatchTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(CTFMatchTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(CTFMatchParticipant participant, CTFMatchTeam team) {
        context.getTopbar().linkToTeam(participant.getUniqueId(), participant.getTeamId());
    }
    
    @Override
    public void onNewParticipantJoin(CTFMatchParticipant participant, CTFMatchTeam team) {
        context.getTopbar().linkToTeam(participant.getUniqueId(), participant.getTeamId());
    }
    
    @Override
    public void onParticipantQuit(CTFMatchParticipant participant, CTFMatchTeam team) {
        context.updateAliveStatus(participant.getAffiliation());
    }
    
    @Override
    public void onTeamQuit(CTFMatchTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull CTFMatchParticipant participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull CTFMatchParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull CTFMatchParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull CTFMatchParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "CaptureTheFlagMatchStateBase.onParticipantDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull CTFMatchParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, CTFMatchParticipant participant) {
        if (participant.getAffiliation() == CaptureTheFlagMatch.Affiliation.NORTH) {
            event.setRespawnLocation(context.getArena().northSpawn());
        } else {
            event.setRespawnLocation(context.getArena().southSpawn());
        }
    }
}
