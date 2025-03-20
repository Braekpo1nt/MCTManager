package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.ui.timer.Timer;

public class HalftimeBreakState extends WaitingInHubState {
    public HalftimeBreakState(EventManager context) {
        super(context);
    }
    
    @Override
    protected Timer startTimer() {
        return context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getHalftimeBreakDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .titleThreshold(20)
                .titleAudience(Audience.audience(context.getParticipants()))
                .sidebarPrefix(Component.text("Break: ").color(NamedTextColor.YELLOW))
                .onCompletion(() -> {
                    cancelAllTasks();
                    gameManager.removeParticipantsFromHub(context.getParticipants());
                    context.setState(new WaitingInHubState(context));
                })
                .build());
    }
}
