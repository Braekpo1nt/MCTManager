package org.braekpo1nt.mctmanager.games.game.example.states;

import org.braekpo1nt.mctmanager.games.game.example.ExampleParticipant;
import org.braekpo1nt.mctmanager.games.game.example.ExampleTeam;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Used during initialization, does nothing, prevents null pointer exceptions
 */
public class InitialState implements ExampleState {
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(ExampleTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(ExampleTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(ExampleParticipant participant, ExampleTeam team) {
        
    }
    
    @Override
    public void onNewParticipantJoin(ExampleParticipant participant, ExampleTeam team) {
        
    }
    
    @Override
    public void onParticipantQuit(ExampleParticipant participant, ExampleTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(ExampleTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ExampleParticipant participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull ExampleParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull ExampleParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull ExampleParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull ExampleParticipant participant) {
        
    }
}
