package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.games.base.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalTeam;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public class ColossalCombatStateBase implements ColossalCombatState {
    protected final @NotNull ColossalCombatGame context;
    
    public ColossalCombatStateBase(@NotNull ColossalCombatGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        context.closeGates();
        context.resetArena();
    }
    
    @Override
    public void onTeamRejoin(ColossalTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(ColossalTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(ColossalParticipant participant, ColossalTeam team) {
        context.updateRoundSidebar(participant);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            participant.teleport(context.getConfig().getSpectatorSpawn());
            return;
        }
        switch (participant.getAffiliation()) {
            case NORTH -> {
                participant.teleport(context.getConfig().getNorthGate().getSpawn());
            }
            case SOUTH -> {
                participant.teleport(context.getConfig().getSouthGate().getSpawn());
            }
        }
        participant.setGameMode(GameMode.ADVENTURE);
        context.getTopbar().linkToTeam(participant.getUniqueId(), participant.getTeamId());
        context.getTopbar().setKillsAndDeaths(participant.getUniqueId(), participant.getKills(), participant.getDeaths());
        context.updateAliveStatus(participant.getAffiliation());
    }
    
    @Override
    public void onNewParticipantJoin(ColossalParticipant participant, ColossalTeam team) {
        context.updateRoundSidebar(participant);
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            participant.teleport(context.getConfig().getSpectatorSpawn());
            return;
        }
        switch (participant.getAffiliation()) {
            case NORTH -> {
                participant.teleport(context.getConfig().getNorthGate().getSpawn());
            }
            case SOUTH -> {
                participant.teleport(context.getConfig().getSouthGate().getSpawn());
            }
        }
        context.getTopbar().linkToTeam(participant.getUniqueId(), participant.getTeamId());
        context.getTopbar().setKillsAndDeaths(participant.getUniqueId(), participant.getKills(), participant.getDeaths());
        context.updateAliveStatus(participant.getAffiliation());
    }
    
    @Override
    public void onParticipantQuit(ColossalParticipant participant, ColossalTeam team) {
        if (participant.getAffiliation() == Affiliation.SPECTATOR) {
            return;
        }
        context.updateAliveStatus(participant.getAffiliation());
    }
    
    @Override
    public void onTeamQuit(ColossalTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ColossalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull ColossalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull ColossalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull ColossalParticipant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull ColossalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, ColossalParticipant participant) {
        switch (participant.getAffiliation()) {
            case NORTH -> event.setRespawnLocation(context.getConfig().getNorthGate().getSpawn());
            case SOUTH -> event.setRespawnLocation(context.getConfig().getSouthGate().getSpawn());
            case SPECTATOR -> event.setRespawnLocation(context.getConfig().getSpectatorSpawn());
        }
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, ColossalParticipant participant) {
        
    }
}
