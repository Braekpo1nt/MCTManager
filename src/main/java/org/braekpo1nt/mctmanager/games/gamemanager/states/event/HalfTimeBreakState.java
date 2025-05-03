package org.braekpo1nt.mctmanager.games.gamemanager.states.event;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class HalfTimeBreakState extends WaitingInHubState {
    public HalfTimeBreakState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
    }
    
    @Override
    protected Timer startTimer() {
        return context.getTimerManager().start(Timer.builder()
                .duration(eventData.getConfig().getHalftimeBreakDuration())
                .withSidebar(sidebar, "timer")
                .titleThreshold(20)
                .titleAudience(Audience.audience(onlineParticipants.values()))
                .sidebarPrefix(Component.text("Break: ").color(NamedTextColor.YELLOW))
                .onCompletion(() -> {
                    disableTips();
                    context.setState(new WaitingInHubState(context, contextReference, eventData));
                })
                .build());
    }
}
