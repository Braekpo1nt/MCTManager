package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class DescriptionState implements FarmRushState {
    
    protected final @NotNull FarmRushGame context;
    
    public DescriptionState(@NotNull FarmRushGame context) {
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
                .onCompletion(() -> {
//                    context.setState(new StartingState(context));
                })
                .build());
    }
}
