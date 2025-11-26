package org.braekpo1nt.mctmanager.games.game.finalgame.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalGame;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalParticipant;
import org.braekpo1nt.mctmanager.games.game.finalgame.FinalTeam;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FinalStateBase implements FinalState {
    protected final @NotNull FinalGame context;
    
    public FinalStateBase(@NotNull FinalGame context) {
        this.context = context;
    }
    
    @Override
    public void onOpenKitPicker(@NotNull FinalParticipant participant) {
        // do nothing
    }
    
    @Override
    public void cleanup() {
        exit();
    }
    
    @Override
    public void onTeamRejoin(FinalTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(FinalTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(FinalParticipant participant, FinalTeam team) {
        
    }
    
    @Override
    public void onNewParticipantJoin(FinalParticipant participant, FinalTeam team) {
        
    }
    
    @Override
    public void onParticipantQuit(FinalParticipant participant, FinalTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(FinalTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull FinalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull FinalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull FinalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull FinalParticipant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull FinalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(@NotNull PlayerRespawnEvent event, @NotNull FinalParticipant participant) {
        switch (participant.getAffiliation()) {
            case NORTH -> event.setRespawnLocation(context.getConfig().getNorthMap().getSpawn());
            case SOUTH -> event.setRespawnLocation(context.getConfig().getSouthMap().getSpawn());
            case SPECTATOR -> event.setRespawnLocation(context.getConfig().getSpectatorSpawn());
        }
    }
    
    @Override
    public void onParticipantPostRespawn(@Nullable PlayerPostRespawnEvent event, @NotNull FinalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantToggleGlide(@NotNull EntityToggleGlideEvent event, FinalParticipant participant) {
        
    }
}
