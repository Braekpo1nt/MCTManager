package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class CountdownState extends ActiveStateBase {
    public CountdownState(@NotNull ParkourPathwayGame context) {
        super(context);
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getStartingDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting: "))
                .titleAudience(Audience.audience(context.getParticipants().values()))
                .onCompletion(() -> {
                    if (context.getConfig().getGlassBarrierOpenMessage() != null) {
                        context.messageAllParticipants(context.getConfig().getGlassBarrierOpenMessage());
                    }
                    context.openGlassBarrier();
                    context.setState(new ActiveState(context));
                })
                .build());
    }
}
