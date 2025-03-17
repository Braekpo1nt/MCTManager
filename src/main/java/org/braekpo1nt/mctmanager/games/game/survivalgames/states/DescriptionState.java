package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

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
                .onCompletion(() -> context.setState(new StartingState(context)))
                .build());
    }
    
    private void onTeamJoin(Team team) {
        if (context.getTeams().containsKey(team.getTeamId())) {
            return;
        }
        context.getTeams().put(team.getTeamId(), new SurvivalGamesTeam(team, 0));
        context.createPlatformsAndTeleportTeams();
        context.getTopbar().addTeam(team.getTeamId(), team.getColor());
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        onTeamJoin(team);
        context.initializeParticipant(participant);
        context.createPlatformsAndTeleportTeams();
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        context.initializeGlowing(participant);
    }
    
    @Override
    public void onParticipantQuit(SurvivalGamesParticipant participant) {
        context.getParticipants().remove(participant.getUniqueId());
        SurvivalGamesTeam team = context.getTeams().get(participant.getTeamId());
        context.updateAliveCount(team);
        context.getTopbar().unlinkFromTeam(participant.getUniqueId());
        resetParticipant(participant);
        context.onTeamQuit(context.getTeams().get(participant.getTeamId()));
    }
    
    @Override
    public void resetParticipant(Participant participant) {
        context.getTeams().get(participant.getTeamId()).removeParticipant(participant.getUniqueId());
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
    public void onParticipantDeath(PlayerDeathEvent event) {
        Main.debugLog(LogType.CANCEL_PLAYER_DEATH_EVENT, "SurvivalGamesGame.DescriptionState.onPlayerDeath() cancelled");
        event.setCancelled(true);
    }
}
