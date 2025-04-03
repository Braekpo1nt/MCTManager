package org.braekpo1nt.mctmanager.games.game.footrace.states;

import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceTeam;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class InitialState implements FootRaceState{
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(FootRaceTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(FootRaceTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(FootRaceParticipant participant, FootRaceTeam team) {
        
    }
    
    @Override
    public void onNewParticipantJoin(FootRaceParticipant participant, FootRaceTeam team) {
        
    }
    
    @Override
    public void onParticipantQuit(FootRaceParticipant participant, FootRaceTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(FootRaceTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull FootRaceParticipant participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull FootRaceParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull FootRaceParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull FootRaceParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull FootRaceParticipant participant) {
        
    }
}
