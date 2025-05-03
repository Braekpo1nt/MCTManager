package org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.PlayingFinalGameState;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class ToFinalGameDelayState extends DelayState {
    private final Timer timer;
    
    public ToFinalGameDelayState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
        this.timer = context.getTimerManager().start(Timer.builder()
                .duration(eventData.getConfig().getStartingGameDuration())
                .withSidebar(sidebar, "timer")
                .sidebarPrefix(Component.text("Colossal Combat: "))
                .onCompletion(() -> context.setState(new PlayingFinalGameState(context, contextReference, eventData)))
                .build());
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        timer.cancel();
    }
    
    @Override
    public void onSwitchMode() {
        timer.cancel();
    }
}
