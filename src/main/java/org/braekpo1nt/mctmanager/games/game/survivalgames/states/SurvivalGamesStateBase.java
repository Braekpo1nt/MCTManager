package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        context.updateRoundLine();
        initializeGlowing(participant);
        participant.setRespawnLocation(context.getConfig().getPlatformSpawns().getFirst(), true);
    }
    
    @Override
    public void onNewParticipantJoin(SurvivalGamesParticipant participant, SurvivalGamesTeam team) {
        context.getTopbar().linkToTeam(participant.getUniqueId(), participant.getTeamId());
        context.initializeKillCount(participant);
        context.updateAliveCount(team);
        context.updateRoundLine();
        initializeGlowing(participant);
        participant.setRespawnLocation(context.getConfig().getPlatformSpawns().getFirst(), true);
    }
    
    /**
     * Make the participant glow to their teammates, and their teammates glow to them
     * (but don't glow to themselves). Also makes the participant glow to the admins.
     * @param participant the participant to show their teammates to
     */
    private void initializeGlowing(SurvivalGamesParticipant participant) {
        SurvivalGamesTeam team = context.getTeams().get(participant.getTeamId());
        for (SurvivalGamesParticipant teammate : team.getParticipants()) {
            if (!teammate.equals(participant)) {
                if (teammate.isAlive()) {
                    context.getGlowManager().showGlowing(participant, teammate);
                }
                if (participant.isAlive()) {
                    context.getGlowManager().showGlowing(teammate, participant);
                }
            }
        }
        if (!participant.isAlive()) {
            return;
        }
        for (Player admin : context.getAdmins()) {
            context.getGlowManager().showGlowing(admin, participant);
        }
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
    public void onParticipantRespawn(@NotNull PlayerRespawnEvent event, @NotNull SurvivalGamesParticipant participant) {
        event.setRespawnLocation(participant.getLocation());
    }
    
    @Override
    public void onParticipantPostRespawn(@Nullable PlayerPostRespawnEvent event, @NotNull SurvivalGamesParticipant participant) {
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
    }
    
    @Override
    public void onParticipantToggleGlide(@NotNull EntityToggleGlideEvent event, SurvivalGamesParticipant participant) {
        // do nothing
    }
}
