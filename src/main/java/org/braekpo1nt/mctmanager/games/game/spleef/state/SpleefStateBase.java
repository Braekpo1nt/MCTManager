package org.braekpo1nt.mctmanager.games.game.spleef.state;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefTeam;
import org.braekpo1nt.mctmanager.geometry.CompositeGeometry;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class SpleefStateBase implements SpleefState {
    
    protected final @NotNull SpleefGame context;
    
    public SpleefStateBase(@NotNull SpleefGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(SpleefTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(SpleefTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(SpleefParticipant participant, SpleefTeam team) {
        
    }
    
    @Override
    public void onNewParticipantJoin(SpleefParticipant participant, SpleefTeam team) {
        
    }
    
    @Override
    public void onParticipantQuit(SpleefParticipant participant, SpleefTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(SpleefTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull SpleefParticipant participant) {
        CompositeGeometry safetyArea = context.getConfig().getSafetyArea();
        if (safetyArea == null) {
            return;
        }
        if (!safetyArea.contains(event.getFrom().toVector())) {
            participant.teleport(context.getConfig().getStartingLocations().getFirst());
            return;
        }
        if (!safetyArea.contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull SpleefParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull SpleefParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SpleefParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SpleefStateBase.onParticipantDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull SpleefParticipant participant) {
        
    }
    
    @Override
    public void onParticipantBreakBlock(BlockBreakEvent event, SpleefParticipant participant) {
        event.setCancelled(true);
    }
}
