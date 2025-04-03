package org.braekpo1nt.mctmanager.games.game.spleef_new.state;

import org.braekpo1nt.mctmanager.games.game.spleef_new.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef_new.SpleefTeam;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class InitialState implements SpleefState {
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
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull SpleefParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull SpleefParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SpleefParticipant participant) {
        
    }
}
