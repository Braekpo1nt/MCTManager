package org.braekpo1nt.mctmanager.games.game.parkourpathway.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.parkourpathway.ParkourPathwayGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CountDownState extends ParkourPathwayStateBase {
    
    private @Nullable Timer timer;
    
    public CountDownState(@NotNull ParkourPathwayGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        timer = context.getTimerManager().start(Timer.builder()
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
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}
