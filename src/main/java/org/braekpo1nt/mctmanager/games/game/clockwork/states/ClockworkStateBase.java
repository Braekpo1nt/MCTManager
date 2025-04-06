package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class ClockworkStateBase implements ClockworkState{
    
    private final @NotNull ClockworkGame context;
    
    public ClockworkStateBase(@NotNull ClockworkGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(ClockworkTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(ClockworkTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(ClockworkParticipant participant, ClockworkTeam team) {
        
    }
    
    @Override
    public void onNewParticipantJoin(ClockworkParticipant participant, ClockworkTeam team) {
        
    }
    
    @Override
    public void onParticipantQuit(ClockworkParticipant participant, ClockworkTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(ClockworkTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ClockworkParticipant participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull ClockworkParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull ClockworkParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull ClockworkParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull ClockworkParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, ClockworkParticipant participant) {
        
    }
}
