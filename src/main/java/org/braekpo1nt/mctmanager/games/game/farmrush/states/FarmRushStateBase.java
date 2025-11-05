package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.farmrush.Arena;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushParticipant;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushTeam;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public abstract class FarmRushStateBase implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    
    public FarmRushStateBase(@NotNull FarmRushGame context) {
        this.context = context;
    }
    
    @Override
    public void onParticipantCloseInventory(InventoryCloseEvent event, FarmRushParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantPlaceBlock(BlockPlaceEvent event, FarmRushParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantOpenInventory(InventoryOpenEvent event, FarmRushParticipant participant) {
        // do nothing
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(FarmRushTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(FarmRushTeam team) {
        context.placeArenas(Collections.singletonList(team.getArena()));
    }
    
    @Override
    public void onParticipantRejoin(FarmRushParticipant participant, FarmRushTeam team) {
        if (team.getArena().getBounds().contains(participant.getLocation().toVector())) {
            return;
        }
        participant.teleport(team.getArena().getSpawn());
    }
    
    @Override
    public void onNewParticipantJoin(FarmRushParticipant participant, FarmRushTeam team) {
        participant.getInventory().setContents(context.getConfig().getLoadout());
        participant.teleport(team.getArena().getSpawn());
    }
    
    @Override
    public void onParticipantQuit(FarmRushParticipant participant, FarmRushTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(FarmRushTeam team) {
        
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull FarmRushParticipant participant) {
        Arena arena = context.getTeams().get(participant.getTeamId()).getArena();
        if (!arena.getBounds().contains(event.getFrom().toVector())) {
            participant.teleport(arena.getSpawn());
            return;
        }
        if (!arena.getBounds().contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull FarmRushParticipant participant) {
        BoundingBox bounds = context.getTeams().get(participant.getTeamId()).getArena().getBounds();
        if (!bounds.contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull FarmRushParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull FarmRushParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "FarmRushState.onPlayerDamage()");
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull FarmRushParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(@NotNull PlayerRespawnEvent event, @NotNull FarmRushParticipant participant) {
        FarmRushTeam team = context.getTeams().get(participant.getTeamId());
        event.setRespawnLocation(team.getArena().getSpawn());
    }
    
    @Override
    public void onParticipantPostRespawn(@Nullable PlayerPostRespawnEvent event, @NotNull FarmRushParticipant participant) {
        
    }
    
    @Override
    public void onParticipantToggleGlide(@NotNull EntityToggleGlideEvent event, FarmRushParticipant participant) {
        // do nothing
    }
    
    @Override
    public void showMaterialGui(FarmRushParticipant participant) {
        context.showMaterialGui(participant);
    }
}
