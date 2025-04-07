package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class BreatherState extends ClockworkStateBase {
    public BreatherState(@NotNull ClockworkGame context) {
        super(context);
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getBreatherDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Clock chimes in: "))
                .onCompletion(() -> context.setState(new ClockChimeState(context)))
                .name("startBreatherDelay")
                .build());
    }
}
