package org.braekpo1nt.mctmanager.games.game.mecha.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.mecha.MechaGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class StartingState extends DescriptionState {
    
    public StartingState(@NotNull MechaGame context) {
        super(context);
    }
    
    @Override
    protected void startTimer() {
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getStartDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .sidebarPrefix(Component.text("Starting: "))
                .titleAudience(Audience.audience(context.getParticipants()))
                .onCompletion(() -> {
                    context.setState(new ActiveState(context));
                })
                .build());
    }
}
