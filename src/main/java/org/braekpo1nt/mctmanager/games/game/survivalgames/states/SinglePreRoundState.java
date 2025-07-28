package org.braekpo1nt.mctmanager.games.game.survivalgames.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.survivalgames.SurvivalGamesGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

/**
 * Used when there is only one round in the game
 */
public class SinglePreRoundState extends PreRoundState {
    
    private Timer timer;
    
    public SinglePreRoundState(@NotNull SurvivalGamesGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        initializeRound();
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withSidebar(context.getAdminSidebar(), "timer")
                .withTopbar(context.getTopbar())
                .topbarPrefix(Component.text("Starting: "))
                .sidebarPrefix(Component.text("Starting: "))
                .onCompletion(() -> context.setState(new GracePeriodState(context)))
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
}
