package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourParticipant;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourTeam;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class ParkourPathwayStateBase implements ParkourPathwayState {
    
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
        
    }
    
    @Override
    public void onNewParticipantJoin(ParkourParticipant participant, ParkourTeam team) {
        
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
        
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull ParkourParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, ParkourParticipant participant) {
        
    }
}
