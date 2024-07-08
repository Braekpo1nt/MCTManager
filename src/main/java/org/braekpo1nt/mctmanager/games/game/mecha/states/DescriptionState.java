package org.braekpo1nt.mctmanager.games.game.mecha.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.mecha.MechaGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DescriptionState implements MechaState {
    
    protected final @NotNull MechaGame context;
    
    public DescriptionState(@NotNull MechaGame context) {
        this.context = context;
        startTimer();
    }
    
    protected void startTimer() {
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    context.setState(new StartingState(context));
                })
                .build());
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        if (!context.getLivingMembers().containsKey(teamId)) {
            NamedTextColor color = context.getGameManager().getTeamNamedTextColor(teamId);
            context.getTopbar().addTeam(teamId, color);
        }
        initializeParticipant(participant);
        context.updateAliveCount(teamId);
        context.createPlatformsAndTeleportTeams();
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        context.getParticipants().remove(participant);
        UUID participantUUID = participant.getUniqueId();
        String teamId = context.getGameManager().getTeamName(participantUUID);
        Integer oldLivingMembers = context.getLivingMembers().get(teamId);
        if (oldLivingMembers != null) {
            context.getLivingMembers().put(teamId, Math.max(0, oldLivingMembers - 1));
            context.updateAliveCount(teamId);
        }
        context.getLivingPlayers().remove(participantUUID);
        context.getKillCounts().remove(participantUUID);
        context.getDeathCounts().remove(participantUUID);
        context.getSidebar().removePlayer(participant);
        context.getTopbar().hidePlayer(participantUUID);
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        context.getParticipants().add(participant);
        context.getLivingPlayers().add(participant.getUniqueId());
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        context.getLivingMembers().putIfAbsent(teamId, 0);
        int oldAliveCount = context.getLivingMembers().get(teamId);
        context.getLivingMembers().put(teamId, oldAliveCount + 1);
        context.getSidebar().addPlayer(participant);
        context.getTopbar().showPlayer(participant);
        context.getTopbar().linkToTeam(participant.getUniqueId(), teamId);
        context.updateAliveCount(teamId);
        context.initializeKillCount(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    @Override
    public void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        context.getSidebar().removePlayer(participant.getUniqueId());
        context.getTopbar().hidePlayer(participant.getUniqueId());
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            return;
        }
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        
    }
    
    @Override
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        
    }
    
    @Override
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        
    }
}
