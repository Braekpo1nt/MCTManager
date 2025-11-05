package org.braekpo1nt.mctmanager.games.game.spleef.state;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.geometry.CompositeGeometry;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

public abstract class SpleefStateBase implements SpleefState {
    
    protected final @NotNull SpleefGame context;
    
    public SpleefStateBase(@NotNull SpleefGame context) {
        this.context = context;
    }
    
    @Override
    public void cleanup() {
        
    }
    
    @Override
    public void onTeamRejoin(SpleefTeam team) {
        
    }
    
    @Override
    public void onNewTeamJoin(SpleefTeam team) {
        
    }
    
    @Override
    public void onParticipantRejoin(SpleefParticipant participant, SpleefTeam team) {
        participant.setGameMode(GameMode.ADVENTURE);
        context.teleportToRandomStartingPosition(participant);
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(context.getCurrentRound()))
                .append(Component.text("/"))
                .append(Component.text(context.getConfig().getRounds()));
        context.getSidebar().updateLine(participant.getUniqueId(), "round", roundLine);
    }
    
    @Override
    public void onNewParticipantJoin(SpleefParticipant participant, SpleefTeam team) {
        participant.setGameMode(GameMode.ADVENTURE);
        context.teleportToRandomStartingPosition(participant);
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(context.getCurrentRound()))
                .append(Component.text("/"))
                .append(Component.text(context.getConfig().getRounds()));
        context.getSidebar().updateLine(participant.getUniqueId(), "round", roundLine);
    }
    
    @Override
    public void onParticipantQuit(SpleefParticipant participant, SpleefTeam team) {
        
    }
    
    @Override
    public void onTeamQuit(SpleefTeam team) {
        
    }
    
    /**
     * @return the number of participants who are alive in this round
     */
    protected long getAliveCount() {
        return context.getParticipants().values().stream().filter(SpleefParticipant::isAlive).count();
    }
    
    protected void updateAliveCount(long aliveCount) {
        Component alive = Component.empty()
                .append(Component.text("Alive: "))
                .append(Component.text(aliveCount));
        context.getSidebar().updateLine("alive", alive);
        context.getAdminSidebar().updateLine("alive", alive);
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull SpleefParticipant participant) {
        CompositeGeometry safetyArea = context.getConfig().getSafetyArea();
        if (safetyArea == null) {
            return;
        }
        if (!safetyArea.contains(event.getFrom().toVector())) {
            participant.teleport(context.getConfig().getStartingLocations().getFirst());
            return;
        }
        if (!safetyArea.contains(event.getTo().toVector())) {
            event.setCancelled(true);
        }
    }
    
    @Override
    public void onParticipantTeleport(@NotNull PlayerTeleportEvent event, @NotNull SpleefParticipant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantInteract(@NotNull PlayerInteractEvent event, @NotNull SpleefParticipant participant) {
        
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull SpleefParticipant participant) {
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SpleefStateBase.onParticipantDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(@NotNull PlayerDeathEvent event, @NotNull SpleefParticipant participant) {
        
    }
    
    @Override
    public void onParticipantRespawn(@NotNull PlayerRespawnEvent event, @NotNull SpleefParticipant participant) {
        event.setRespawnLocation(context.getRandomStartingPosition());
    }
    
    @Override
    public void onParticipantPostRespawn(PlayerPostRespawnEvent event, @NotNull SpleefParticipant participant) {
        participant.setGameMode(GameMode.SPECTATOR);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearInventory(participant);
    }
    
    @Override
    public void onParticipantBreakBlock(BlockBreakEvent event, SpleefParticipant participant) {
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantToggleGlide(@NotNull EntityToggleGlideEvent event, SpleefParticipant participant) {
        // do nothing
    }
}
