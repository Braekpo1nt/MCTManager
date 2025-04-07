package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.game.clockwork.Wedge;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.game.clockwork_old.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.game.clockwork_old.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
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
                        onTeamWinsRound(livingcontext.getTeams().getFirst());
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
    
    private void killParticipants(Collection<ClockworkParticipant> participantsToKill) {
        // teams which were already dead
        List<ClockworkTeam> existingDeadTeams = context.getTeams().values().stream()
                .filter(ClockworkTeam::isDead).toList();
        // participants who will be left alive once participantsToKill are killed
        List<ClockworkParticipant> newLivingParticipants = participants.values().stream()
                .filter(ClockworkParticipant::isAlive)
                .filter(p -> !participantsToKill.contains(p))
                .toList();
        
        for (ClockworkParticipant toKill : participantsToKill) {
            toKill.setGameMode(GameMode.SPECTATOR);
            toKill.getInventory().clear();
            ParticipantInitializer.clearStatusEffects(toKill);
            ParticipantInitializer.resetHealthAndHunger(toKill);
            toKill.setAlive(false);
            plugin.getServer().sendMessage(Component.empty()
                    .append(toKill.displayName())
                    .append(Component.text(" was claimed by time")));
            String killedTeamId = toKill.getTeamId();
            
            // award living participants start
            List<ClockworkParticipant> awardedParticipants = newLivingParticipants.stream()
                    .filter(p -> !p.getTeamId().equals(killedTeamId))
                    .toList();
            context.awardPariticpantPoints(awardedParticipants, config.getPlayerEliminationScore());
            // award living participants end
        }
        // who are now dead, which weren't at the start of this method
        List<ClockworkTeam> newlyKilledTeams = context.getTeams().values().stream()
                .filter(t -> !existingDeadTeams.contains(t))
                .filter(ClockworkTeam::isDead)
                .toList();
        if (newlyKilledTeams.isEmpty()) {
            return;
        }
        List<ClockworkTeam> livingTeams = context.getTeams().values().stream()
                .filter(ClockworkTeam::isAlive)
                .filter(t -> !newlyKilledTeams.contains(t))
                .toList();
        for (ClockworkTeam newlyKilledTeam : newlyKilledTeams) {
            newlyKilledTeam.sendMessage(Component.empty()
                    .append(newlyKilledTeam.getFormattedDisplayName())
                    .append(Component.text(" has been eliminated"))
                    .color(NamedTextColor.DARK_RED));
            Audience.audience(context.getTeams().values().stream().filter(p ->
                            !p.getTeamId().equals(newlyKilledTeam.getTeamId()))
                    .toList()).sendMessage(Component.empty()
                    .append(newlyKilledTeam.getFormattedDisplayName())
                    .append(Component.text(" has been eliminated"))
                    .color(NamedTextColor.GREEN));
            
            context.awardTeamPoints(livingTeams, config.getTeamEliminationScore());
        }
        boolean allTeamsAreDead = context.getTeams().values().stream().noneMatch(ClockworkTeam::isAlive);
        if (allTeamsAreDead) {
            onAllTeamsLoseRound();
        }
    }
    
    private void onAllTeamsLoseRound() {
        context.messageAllParticipants(Component.text("All teams have been eliminated.")
                .color(NamedTextColor.DARK_RED));
        roundIsOver();
    }
}
