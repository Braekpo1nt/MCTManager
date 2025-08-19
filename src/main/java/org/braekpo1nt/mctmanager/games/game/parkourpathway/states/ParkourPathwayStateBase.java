package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourTeam;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ParkourPathwayStateBase implements ParkourPathwayState {
    
    protected final @NotNull ParkourPathwayGame context;
    
    public ParkourPathwayStateBase(@NotNull ParkourPathwayGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(ParkourTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(ParkourTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(ParkourParticipant participant, ParkourTeam team) {
        participant.teleport(context.getConfig().getStartingLocation());
        context.giveBoots(participant);
        participant.addPotionEffect(context.getINVISIBILITY());
        context.updateCheckpointSidebar(participant);
    }
    
    @Override
    public void onNewParticipantJoin(ParkourParticipant participant, ParkourTeam team) {
        participant.teleport(context.getConfig().getStartingLocation());
        context.giveBoots(participant);
        participant.addPotionEffect(context.getINVISIBILITY());
        context.updateCheckpointSidebar(participant);
    }
    
    @Override
    public void onParticipantQuit(ParkourParticipant participant, ParkourTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(ParkourTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ParkourParticipant participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull ParkourParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull ParkourParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull ParkourParticipant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull ParkourParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, ParkourParticipant participant) {
        Location respawn = context.getConfig()
                .getPuzzle(participant.getCurrentPuzzle())
                .getCheckPoints().get(participant.getCurrentPuzzleCheckpoint())
                .getRespawn();
        event.setRespawnLocation(respawn);
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, ParkourParticipant participant) {
        
    }
    
    @Override
    public void onParticipantToggleGlide(@NotNull EntityToggleGlideEvent event, ParkourParticipant participant) {
        // do nothing
    }
}
