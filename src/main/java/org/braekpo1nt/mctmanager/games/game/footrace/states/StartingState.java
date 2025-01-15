package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class StartingState extends DescriptionState {
    
    public StartingState(@NotNull FootRaceGame context) {
        super(context);
    }
    
    @Override
    protected void startTimer() {
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting: "))
                .titleAudience(Audience.audience(context.getParticipants().values()))
                .onCompletion(() -> {
                    if (context.getConfig().useLegacy()) {
                        context.setState(new ActiveStateLegacy(context));
                    } else {
                        context.setState(new ActiveState(context));
                    }
                })
                .build());
    }
}
