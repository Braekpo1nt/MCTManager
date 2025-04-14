package org.braekpo1nt.mctmanager.games.game.capturetheflag.states;

import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFParticipant;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CTFTeam;
import org.braekpo1nt.mctmanager.games.game.capturetheflag.CaptureTheFlagGame;
import org.braekpo1nt.mctmanager.participant.Team;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public abstract class CaptureTheFlagStateBase implements CaptureTheFlagState {
    
    protected final @NotNull CaptureTheFlagGame context;
    
    protected CaptureTheFlagStateBase(@NotNull CaptureTheFlagGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        // do nothing
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull CTFParticipant participant) {
        event.getDrops().clear();
        event.setDroppedExp(0);
    }
    
    @Override
    public void onTeamRejoin(CTFTeam team) {
        context.getQuitTeams().remove(team.getTeamId());
    }
    
    @Override
    public void onNewTeamJoin(CTFTeam team) {
        context.getRoundManager().regenerateRounds(Team.getTeamIds(context.getTeams()),
                context.getConfig().getArenas().size());
    }
    
    @Override
    public void onTeamQuit(CTFTeam team) {
        context.getQuitTeams().put(team.getTeamId(), team);
        context.getRoundManager().regenerateRounds(Team.getTeamIds(context.getTeams()),
                context.getConfig().getArenas().size());
        context.updateRoundLine();
    }
    
    @Override
    public void onParticipantRejoin(CTFParticipant participant, CTFTeam team) {
        participant.teleport(context.getConfig().getSpawnObservatory());
        context.updateRoundLine(participant.getUniqueId());
    }
    
    @Override
    public void onNewParticipantJoin(CTFParticipant participant, CTFTeam team) {
        participant.teleport(context.getConfig().getSpawnObservatory());
        context.updateRoundLine(participant.getUniqueId());
    }
    
    @Override
    public void onParticipantRespawn(PlayerRespawnEvent event, CTFParticipant participant) {
        event.setRespawnLocation(context.getConfig().getSpawnObservatory());
    }
    
    @Override
    public void onParticipantFoodLevelChange(@NotNull FoodLevelChangeEvent event, @NotNull CTFParticipant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantClickInventory(@NotNull InventoryClickEvent event, @NotNull CTFParticipant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull CTFParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(CTFParticipant participant, CTFTeam team) {
        // do nothing
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull CTFParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull CTFParticipant participant) {
        // do nothing
    }
}
