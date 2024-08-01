package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.PodiumState;
import org.braekpo1nt.mctmanager.ui.timer.Timer;

public class ToPodiumDelayState extends DelayState {
    public ToPodiumDelayState(EventManager context) {
        super(context);
        context.initializeParticipantsAndAdmins();
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
}
