package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiPreRoundState extends PreRoundState {
    
    private @Nullable Timer timer;
    
    public MultiPreRoundState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        initializeRound();
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Round Starting: "))
                .sidebarPrefix(Component.text("Round Starting: "))
                .onCompletion(() -> context.setState(new GracePeriodState(context)))
                .build());
        context.titleAllParticipants(UIUtils.roundXTitle(context.getCurrentRound()));
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}
