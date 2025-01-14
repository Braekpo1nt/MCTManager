package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DescriptionState implements SurvivalGamesState {
    
    protected final @NotNull SurvivalGamesGame context;
    
    public DescriptionState(@NotNull SurvivalGamesGame context) {
        this.context = context;
        startTimer();
    }
    
    protected void startTimer() {
        context.messageAllParticipants(context.getConfig().getDescription());
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
        context.getDeadPlayers().remove(participant.getUniqueId());
        String teamId = context.getGameManager().getTeamId(participant.getUniqueId());
        if (!context.getLivingMembers().containsKey(teamId)) {
            NamedTextColor color = context.getGameManager().getTeamColor(teamId);
            context.getTopbar().addTeam(teamId, color);
        }
        initializeParticipant(participant);
        context.createPlatformsAndTeleportTeams();
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        context.initializeGlowing(participant);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        context.getParticipants().remove(participant);
        UUID participantUUID = participant.getUniqueId();
        String teamId = context.getGameManager().getTeamId(participantUUID);
        Integer oldLivingMembers = context.getLivingMembers().get(teamId);
        if (oldLivingMembers != null) {
            context.getLivingMembers().put(teamId, Math.max(0, oldLivingMembers - 1));
            context.updateAliveCount(teamId);
        }
        context.getLivingPlayers().remove(participantUUID);
        context.getKillCounts().remove(participantUUID);
        context.getDeathCounts().remove(participantUUID);
        context.getTopbar().unlinkFromTeam(participantUUID);
        resetParticipant(participant);
    }
    
    @Override
    public void initializeParticipant(Participant participant) {
        context.getParticipants().add(participant);
        context.getLivingPlayers().add(participant.getUniqueId());
        String teamId = context.getGameManager().getTeamId(participant.getUniqueId());
        context.getLivingMembers().putIfAbsent(teamId, 0);
        int oldAliveCount = context.getLivingMembers().get(teamId);
        context.getLivingMembers().put(teamId, oldAliveCount + 1);
        context.getSidebar().addPlayer(participant);
        context.getTopbar().showPlayer(participant);
        context.getTopbar().linkToTeam(participant.getUniqueId(), teamId);
        context.updateAliveCount(teamId);
        context.initializeKillCount(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        context.getGlowManager().addPlayer(participant);
    }
    
    @Override
    public void resetParticipant(Player participant) {
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        context.getSidebar().removePlayer(participant.getUniqueId());
        context.getTopbar().hidePlayer(participant.getUniqueId());
        context.getGlowManager().removePlayer(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SurvivalGames.DescriptionState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onPlayerDeath(PlayerDeathEvent event) {
        Main.debugLog(LogType.CANCEL_PLAYER_DEATH_EVENT, "SurvivalGamesGame.DescriptionState.onPlayerDeath() cancelled");
        event.setCancelled(true);
    }
}
