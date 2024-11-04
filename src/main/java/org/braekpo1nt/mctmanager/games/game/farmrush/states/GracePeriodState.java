package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.text.format.NamedTextColor;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class GracePeriodState extends ActiveState {
    public GracePeriodState(@NotNull FarmRushGame context) {
        super(context);
    }
    
    @Override
    protected void init() {
        // do nothing
    }
    
    @Override
    protected Timer getTimer() {
        return context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGameDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .timerColor(NamedTextColor.RED)
                .onCompletion(() -> context.setState(new GameOverState(context)))
                .build());
    }
    
    @Override
    protected void checkForWin(FarmRushGame.Team team) {
        // do nothing
    }
}
