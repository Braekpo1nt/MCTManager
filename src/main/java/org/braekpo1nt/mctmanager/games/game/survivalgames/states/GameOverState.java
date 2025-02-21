package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesParticipant;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesTeam;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GameOverState implements SurvivalGamesState {
    private final @NotNull SurvivalGamesGame context;
    
    public GameOverState(@NotNull SurvivalGamesGame context) {
        this.context = context;
        Audience.audience(context.getParticipants().values()).showTitle(UIUtils.gameOverTitle());
        context.getAdminSidebar().addLine("over", "");
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getEndDuration())
                .withSidebar(context.getAdminSidebar(), "over")
                .sidebarPrefix(Component.text("Game Over: "))
                .withTopbar(context.getTopbar())
                .onCompletion(() -> {
                    context.getAdminSidebar().deleteLine("over");
                    context.stop();
                })
                .build());
    }
    
    @Override
    public void onParticipantJoin(Participant participant, Team team) {
        context.getGlowManager().addPlayer(participant);
        context.initializeGlowing(participant);
    }
    
    @Override
    public void onParticipantQuit(SurvivalGamesParticipant participant) {
        context.getParticipants().remove(participant.getUniqueId());
        SurvivalGamesTeam team = context.getTeams().get(participant.getTeamId());
        context.updateAliveCount(team);
        context.getTopbar().unlinkFromTeam(participant.getUniqueId());
        resetParticipant(participant);
    }
    
    @Override
    public void resetParticipant(Participant participant) {
        context.resetParticipant(participant);
    }
    
    @Override
    public void onPlayerDamage(EntityDamageEvent event) {
        if (GameManagerUtils.EXCLUDED_CAUSES.contains(event.getCause())) {
            return;
        }
        Main.debugLog(LogType.CANCEL_ENTITY_DAMAGE_EVENT, "SurvivalGames.GameOverState.onPlayerDamage() cancelled");
        event.setCancelled(true);
    }
    
    @Override
    public void onParticipantDeath(PlayerDeathEvent event) {
        Main.debugLog(LogType.CANCEL_PLAYER_DEATH_EVENT, "SurvivalGamesGame.GameOverState.onPlayerDeath() cancelled");
        event.setCancelled(true);
    }
}
