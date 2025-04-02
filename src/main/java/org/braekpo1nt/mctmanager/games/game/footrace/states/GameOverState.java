package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class GameOverState extends FootRaceStateBase {
    
    public GameOverState(@NotNull FootRaceGame context) {
        super(context);
        Audience.audience(context.getParticipants().values()).showTitle(UIUtils.gameOverTitle());
        context.getSidebar().addLine("over", Component.empty());
        context.getAdminSidebar().addLine("over", Component.empty());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGameOverDuration())
                .withSidebar(context.getSidebar(), "over")
                .withSidebar(context.getAdminSidebar(), "over")
                .sidebarPrefix(Component.text("Game Over: "))
                .onCompletion(() -> {
                    context.getAdminSidebar().deleteLine("over");
                    context.stop();
                })
                .build());
    }
    
}
