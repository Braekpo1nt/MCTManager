package org.braekpo1nt.mctmanager.games.gamemanager.states.event.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.event.EventData;
import org.braekpo1nt.mctmanager.games.gamemanager.states.ContextReference;
import org.braekpo1nt.mctmanager.games.gamemanager.states.event.PlayingGameState;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class StartingGameDelayState extends DelayState {
    private final Timer timer;
    
    public StartingGameDelayState(
            @NotNull GameManager context,
            @NotNull ContextReference contextReference,
            @NotNull EventData eventData,
            @NotNull GameType gameType,
            @NotNull String gameConfigFile) {
        super(context, contextReference, eventData);
        this.timer = context.getTimerManager().start(Timer.builder()
                .duration(eventData.getConfig().getStartingGameDuration())
                .withSidebar(sidebar, "timer")
                .sidebarPrefix(Component.empty()
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(": ")))
                .onCompletion(() -> {
                    context.setState(new PlayingGameState(context, contextReference, eventData, gameType, gameConfigFile));
                })
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
