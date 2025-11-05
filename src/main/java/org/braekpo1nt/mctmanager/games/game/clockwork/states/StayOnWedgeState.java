package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.game.clockwork.Wedge;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StayOnWedgeState extends RoundActiveState {
    private final @NotNull Wedge currentWedge;
    private @Nullable Timer timer;
    
    public StayOnWedgeState(@NotNull ClockworkGame context) {
        super(context);
        this.currentWedge = config.getWedges().get(context.getNumberOfChimes() - 1);
    }
    
    @Override
    public void enter() {
        killParticipantsNotOnWedge();
        List<ClockworkTeam> stillLivingTeams = getLivingTeams();
        if (stillLivingTeams.isEmpty()) {
            Main.logf("stillLivingTeams is empty, all teams lose");
            onAllTeamsLoseRound();
            return;
        }
        timer = context.getTimerManager().start(Timer.builder()
                .duration(config.getStayOnWedgeDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Stay on wedge: "))
                .onCompletion(() -> {
                    Main.logf("StayOnWedgeState timer onCompletion");
                    List<ClockworkTeam> livingTeams = getLivingTeams();
                    if (config.isLongMode()) { // if we should be in long mode
                        if (livingTeams.isEmpty()) { // 0 teams are alive
                            context.getChaosManager().stop();
                            onAllTeamsLoseRound();
                        } else { // at least 1 team is alive
                            context.incrementChaos();
                            context.setState(new BreatherState(context));
                        }
                    } else {
                        if (livingTeams.size() >= 2) {
                            context.incrementChaos();
                            context.setState(new BreatherState(context));
                        } else if (livingTeams.size() == 1) {
                            context.getChaosManager().stop();
                            onTeamWinsRound(livingTeams.getFirst());
                        } else { // 0 teams are alive
                            context.getChaosManager().stop();
                            onAllTeamsLoseRound();
                        }
                    }
                })
                .name("stayOnWedgeDelay")
                .build());
    }
    
    @Override
    public void exit() {
        if (timer != null) {
            timer.cancel();
        }
    }
    
    private void killParticipantsNotOnWedge() {
        List<ClockworkParticipant> participantsToKill = new ArrayList<>();
        for (ClockworkParticipant participant : context.getParticipants().values()) {
            if (participant.isAlive() && !currentWedge.contains(participant.getLocation().toVector())) {
                participantsToKill.add(participant);
            }
        }
        if (participantsToKill.isEmpty()) {
            return;
        }
        killParticipants(participantsToKill);
    }
    
    @Override
    public void cleanup() {
        context.getChaosManager().stop();
    }
    
    private void roundIsOver() {
        cleanup();
        context.setState(new RoundOverState(context));
    }
    
    private void onTeamWinsRound(ClockworkTeam winner) {
        for (Participant participant : context.getParticipants().values()) {
            if (participant.getTeamId().equals(winner.getTeamId())) {
                participant.sendMessage(Component.empty()
                        .append(winner.getFormattedDisplayName())
                        .append(Component.text(" wins this round!"))
                        .color(NamedTextColor.GREEN));
            } else {
                participant.sendMessage(Component.empty()
                        .append(winner.getFormattedDisplayName())
                        .append(Component.text(" wins this round"))
                        .color(NamedTextColor.DARK_RED));
            }
        }
        context.awardPoints(winner, config.getWinRoundScore());
        roundIsOver();
    }
    
    private void onAllTeamsLoseRound() {
        context.messageAllParticipants(Component.text("All teams have been eliminated.")
                .color(NamedTextColor.DARK_RED));
        roundIsOver();
    }
    
    @Override
    public void onParticipantDamage(@NotNull EntityDamageEvent event, @NotNull ClockworkParticipant participant) {
        if (!participant.isAlive()) {
            event.setCancelled(true);
            return;
        }
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
            event.setCancelled(true);
            return;
        }
        event.setDamage(0);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    @Override
    public void onParticipantMove(@NotNull PlayerMoveEvent event, @NotNull ClockworkParticipant participant) {
        if (!participant.isAlive()) {
            return;
        }
        if (!currentWedge.contains(participant.getLocation().toVector())) {
            killParticipants(Collections.singleton(participant));
        }
    }
}
