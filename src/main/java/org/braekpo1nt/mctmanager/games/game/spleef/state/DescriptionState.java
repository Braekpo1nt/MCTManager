package org.braekpo1nt.mctmanager.games.game.spleef.state;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionState extends SpleefStateBase {
    
    private @Nullable Timer timer;
    
    public DescriptionState(@NotNull SpleefGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.messageAllParticipants(context.getConfig().getDescription());
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> {
                    context.setState(new PreRoundState(context));
                })
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}
