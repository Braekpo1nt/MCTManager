package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceTeam;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class DescriptionState extends FootRaceStateBase {
    
    protected final @NotNull FootRaceGame context;
    
    public DescriptionState(@NotNull FootRaceGame context) {
        this.context = context;
        startTimer();
    }
    
    protected void startTimer() {
        context.messageAllParticipants(context.getConfig().getDescription());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> context.setState(new StartingState(context)))
                .build());
    }
    
    @Override
    public void onParticipantJoin(Participant newParticipant, Team team) {
        context.onTeamJoin(team);
        initializeParticipant(newParticipant);
        FootRaceParticipant participant = context.getParticipants().get(newParticipant.getUniqueId());
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        context.getSidebar().updateLine(participant.getUniqueId(), "lap", Component.empty()
                .append(Component.text("Lap: "))
                .append(Component.text(participant.getLap()))
                .append(Component.text("/"))
                .append(Component.text(context.getConfig().getLaps())));
        context.updateStandings();
        context.displayStandings();
        context.displayScore(context.getParticipants().get(participant.getUniqueId()));
        context.displayScore(context.getTeams().get(team.getTeamId()));
        participant.sendMessage(context.getConfig().getDescription());
    }
    
    @Override
    public void onParticipantQuit(FootRaceParticipant participant, FootRaceTeam team) {
        resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
        context.getStandings().remove(participant);
        context.updateStandings();
        context.displayStandings();
        context.onTeamQuit(team);
    }
    
    @Override
    public void initializeParticipant(Participant participant) {
        context.initializeParticipant(participant);
    }
    
    @Override
    public void resetParticipant(FootRaceParticipant participant) {
        context.resetParticipant(participant);
    }
    
    @Override
    public void onParticipantMove(FootRaceParticipant participant) {
        // do nothing
    }
}
