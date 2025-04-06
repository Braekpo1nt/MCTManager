package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.game.clockwork.Wedge;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.game.clockwork_old.ClockworkRoundParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StayOnWedgeState extends ClockworkStateBase {
    private final @NotNull ClockworkConfig config;
    
    public StayOnWedgeState(@NotNull ClockworkGame context) {
        super(context);
        this.config = context.getConfig();
        context.getTimerManager().start(Timer.builder()
                .duration(config.getStayOnWedgeDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Stay on wedge: "))
                .onCompletion(() -> {
                    List<ClockworkTeam> livingTeams = context.getTeams().values().stream()
                            .filter(ClockworkTeam::isAlive).toList();
                    if (livingTeams.size() == 1) {
                        onTeamWinsRound(livingTeams.getFirst());
                    } else {
                        context.incrementChaos();
                        context.setState(new BreatherState(context));
                    }
                })
                .name("startStayOnWedgeDelay")
                .build());
        killParticipantsNotOnWedge();
    }
    
    private void killParticipantsNotOnWedge() {
        List<ClockworkParticipant> participantsToKill = new ArrayList<>();
        Wedge currentWedge = config.getWedges().get(context.getNumberOfChimes() - 1);
        for (ClockworkParticipant participant : context.getParticipants().values()) {
            if (participant.isAlive()) {
                if (!currentWedge.contains(participant.getLocation().toVector())) {
                    participantsToKill.add(participant);
                }
            }
        }
        if (participantsToKill.isEmpty()) {
            return;
        }
        killParticipants(participantsToKill);
    }
    
    private void roundIsOver() {
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
}
