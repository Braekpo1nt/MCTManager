package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceTeam;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class GameOverState implements FootRaceState {
    
    private final @NotNull FootRaceGame context;
    
    public GameOverState(@NotNull FootRaceGame context) {
        this.context = context;
        Audience.audience(context.getParticipants().values()).showTitle(UIUtils.gameOverTitle());
        context.getSidebar().addLine("over", Component.empty());
        context.getAdminSidebar().addLine("over", Component.empty());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGameOverDuration())
                .withSidebar(context.getSidebar(), "over")
                .withSidebar(context.getAdminSidebar(), "over")
                .sidebarPrefix(Component.text("Game Over: "))
                .onCompletion(() -> {
                    context.getAdminSidebar().deleteLine("over");
                    context.stop();
                })
                .build());
    }
    
    @Override
    public void onParticipantJoin(Participant newParticipant, Team team) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(FootRaceParticipant participant, FootRaceTeam team) {
        context.getQuitDatas().put(participant.getUniqueId(), participant.getQuitData());
        resetParticipant(participant);
        context.getParticipants().remove(participant.getUniqueId());
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
