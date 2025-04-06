package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class DescriptionState extends ClockworkStateBase {
    public DescriptionState(@NotNull ClockworkGame context) {
        super(context);
        context.messageAllParticipants(context.getConfig().getDescription());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    context.setState(new PreRoundState(context));
                })
                .build());
    }
}
