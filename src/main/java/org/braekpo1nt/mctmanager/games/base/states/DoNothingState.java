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

/**
 * A convenience interface to reduce boilerplate in InitialStates
 * @param <P> the participant type
 * @param <T> the team type
 */
public interface DoNothingState<P extends ParticipantData, T extends ScoredTeamData<P>> extends GameStateBase<P, T> {
    @Override
    default void cleanup() {
        
    }
    
    @Override
    default void exit() {
        
    }
    
    @Override
    default void enter() {
        
    }
    
    @Override
    default void onTeamRejoin(T team) {
        
    }
    
    @Override
    default void onNewTeamJoin(T team) {
        
    }
    
    @Override
    default void onParticipantRejoin(P participant, T team) {
        
    }
    
    @Override
    default void onNewParticipantJoin(P participant, T team) {
        
    }
    
    @Override
    default void onParticipantQuit(P participant, T team) {
        
    }
    
    @Override
    default void onTeamQuit(T team) {
        
    }
    
    @Override
    default void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull P participant) {
        
    }
    
    @Override
    default void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull P participant) {
        
    }
    
    @Override
    default void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull P participant) {
        
    }
    
    @Override
    default void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull P participant) {
        
    }
    
    @Override
    default void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull P participant) {
        
    }
    
    @Override
    default void onParticipantRespawn(@NotNull PlayerRespawnEvent event, @NotNull P participant) {
        
    }
    
    @Override
    default void onParticipantPostRespawn(PlayerPostRespawnEvent event, @NotNull P participant) {
        
    }
    
    @Override
    default void onParticipantToggleGlide(@NotNull EntityToggleGlideEvent event, P participant) {
        
    }
}
