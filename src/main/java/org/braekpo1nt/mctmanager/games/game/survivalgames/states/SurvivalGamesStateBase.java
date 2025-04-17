package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.GameMode;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public abstract class SurvivalGamesStateBase implements SurvivalGamesState {
    
    protected final @NotNull SurvivalGamesGame context;
    
    public SurvivalGamesStateBase(@NotNull SurvivalGamesGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(SurvivalGamesTeam team) {
        context.getTopbar().addTeam(team.getTeamId(), team.getColor());
    }
    
    @Override
    public void onNewTeamJoin(SurvivalGamesTeam team) {
        context.getTopbar().addTeam(team.getTeamId(), team.getColor());
    }
    
    @Override
    public void onParticipantRejoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        context.getTopbar().linkToTeam(participant.getUniqueId(), participant.getTeamId());
        context.initializeKillCount(participant);
        context.updateAliveCount(team);
        context.initializeGlowing(participant);
    }
    
    @Override
    public void onNewParticipantJoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        context.getTopbar().linkToTeam(participant.getUniqueId(), participant.getTeamId());
        context.initializeKillCount(participant);
        context.updateAliveCount(team);
        context.initializeGlowing(participant);
    }
    
    @Override
    public void onParticipantQuit(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        // do nothing
    }
    
    @Override
    public void onTeamQuit(SurvivalGamesTeam team) {
        context.getTopbar().removeTeam(team.getTeamId());
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
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull SurvivalGamesParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, SurvivalGamesParticipant participant) {
        event.setRespawnLocation(participant.getLocation());
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, SurvivalGamesParticipant participant) {
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
    }
}
