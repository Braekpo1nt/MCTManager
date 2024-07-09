package org.braekpo1nt.mctmanager.games.game.mecha.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.mecha.MechaGame;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GameOverState implements MechaState {
    private final @NotNull MechaGame context;
    
    public GameOverState(@NotNull MechaGame context) {
        this.context = context;
        Audience.audience(context.getParticipants()).showTitle(UIUtils.gameOverTitle());
        context.getAdminSidebar().addLine("over", "");
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getEndDuration())
                .withSidebar(context.getAdminSidebar(), "over")
                .sidebarPrefix(Component.text("Game over: "))
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Game over: "))
                .onCompletion(() -> {
                    context.getAdminSidebar().deleteLine("over");
                    context.stop();
                })
                .build());
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        // do nothing
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
        context.getTopbar().unlinkFromTeam(participantUUID);
        resetParticipant(participant);
    }
    
    @Override
    public void initializeParticipant(Player participant) {
        // not used
    }
    
    @Override
    public void resetParticipant(Player participant) {
        context.resetParticipant(participant);
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
        event.setCancelled(true);
    }
}
