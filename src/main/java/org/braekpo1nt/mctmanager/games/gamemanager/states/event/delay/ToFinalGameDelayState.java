package org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.PlayingFinalGameState;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class ToFinalGameDelayState extends DelayState {
    private final Timer timer;
    
    public ToFinalGameDelayState(@NotNull GameManager context, @NotNull ContextReference contextReference, @NotNull EventData eventData) {
        super(context, contextReference, eventData);
        this.timer = Timer.builder()
                .duration(eventData.getConfig().getStartingGameDuration())
                .withSidebar(sidebar, "timer")
                .sidebarPrefix(Component.text("Colossal Combat: "))
                .onCompletion(() -> context.setState(new PlayingFinalGameState(context, contextReference, eventData)))
                .build();
    }
    
    @Override
    public void enter() {
        context.getTimerManager().start(timer);
    }
    
    @Override
    public void exit() {
        timer.cancel();
    }
    
}
