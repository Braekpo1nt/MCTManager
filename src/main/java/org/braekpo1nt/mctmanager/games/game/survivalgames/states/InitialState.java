package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class InitialState implements SurvivalGamesState {
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(SurvivalGamesTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(SurvivalGamesTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        
    }
    
    @Override
    public void onNewParticipantJoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        
    }
    
    @Override
    public void onParticipantQuit(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(SurvivalGamesTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull SurvivalGamesParticipant participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull SurvivalGamesParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull SurvivalGamesParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SurvivalGamesParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull SurvivalGamesParticipant participant) {
        
    }
}
