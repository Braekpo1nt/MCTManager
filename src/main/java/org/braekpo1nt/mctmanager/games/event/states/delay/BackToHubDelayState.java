package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.HalftimeBreakState;
import org.braekpo1nt.mctmanager.games.event.states.WaitingInHubState;
import org.braekpo1nt.mctmanager.ui.timer.Timer;

public class BackToHubDelayState extends DelayState {
    public BackToHubDelayState(EventManager context) {
        super(context);
        context.initializeParticipantsAndAdmins();
        context.getSidebar().updateLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().updateLine("currentGame", context.getCurrentGameLine());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getBackToHubDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Back to Hub: "))
                .onCompletion(() -> {
                    if (context.isItHalfTime()) {
                        context.setState(new HalftimeBreakState(context));
                    } else {
                        context.setState(new WaitingInHubState(context));
                    }
                })
                .build());
    }
}
