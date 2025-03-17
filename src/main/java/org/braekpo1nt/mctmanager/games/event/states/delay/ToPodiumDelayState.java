package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.PodiumState;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;

import java.util.Collection;

public class ToPodiumDelayState extends DelayState {
    public ToPodiumDelayState(EventManager context) {
        super(context);
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getBackToHubDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Heading to Podium: "))
                .onCompletion(() -> {
                    context.setState(new PodiumState(context));
                })
                .build());
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "ToPodiumDelayState updatePersonalScores()----");
        // do nothing
    }
    
    @Override
    public <T extends Team> void updateTeamScores(Collection<T> updateTeams) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "ToPodiumDelayState updateTeamScores()----");
        // do nothing
    }
}
