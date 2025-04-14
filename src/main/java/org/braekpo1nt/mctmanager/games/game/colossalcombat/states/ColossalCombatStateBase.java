package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import org.braekpo1nt.mctmanager.games.experimental.Affiliation;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalParticipant;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalTeam;
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
        
    }
    
    @Override
    public void onTeamRejoin(ColossalTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(ColossalTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(ColossalParticipant participant, ColossalTeam team) {
        if (participant.getAffiliation() == Affiliation.NORTH) {
            participant.teleport(context.getConfig().getNorthGate().getSpawn());
        } else {
            participant.teleport(context.getConfig().getSouthGate().getSpawn());
        }
    }
    
    @Override
    public void onNewParticipantJoin(ColossalParticipant participant, ColossalTeam team) {
        if (participant.getAffiliation() == Affiliation.NORTH) {
            participant.teleport(context.getConfig().getNorthGate().getSpawn());
        } else {
            participant.teleport(context.getConfig().getSouthGate().getSpawn());
        }
    }
    
    @Override
    public void onParticipantQuit(ColossalParticipant participant, ColossalTeam team) {
        
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
        
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull ColossalParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, ColossalParticipant participant) {
        
    }
}
