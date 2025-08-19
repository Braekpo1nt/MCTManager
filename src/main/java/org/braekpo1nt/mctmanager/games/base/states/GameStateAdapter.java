package org.braekpo1nt.mctmanager.games.base.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.participant.ParticipantData;
import org.braekpo1nt.mctmanager.participant.ScoredTeamData;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public abstract class GameStateAdapter<P extends ParticipantData, T extends ScoredTeamData<P>> implements GameStateBase<P, T> {
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(T team) {
        
    }
    
    @Override
    public void onNewTeamJoin(T team) {
        
    }
    
    @Override
    public void onParticipantRejoin(P participant, T team) {
        
    }
    
    @Override
    public void onNewParticipantJoin(P participant, T team) {
        
    }
    
    @Override
    public void onParticipantQuit(P participant, T team) {
        
    }
    
    @Override
    public void onTeamQuit(T team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull P participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull P participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull P participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull P participant) {
        
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull P participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, P participant) {
        
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, P participant) {
        
    }
    
    @Override
    public void onParticipantToggleGlide(@NotNull EntityToggleGlideEvent event, P participant) {
        
    }
}
