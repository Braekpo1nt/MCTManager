package org.braekpo1nt.mctmanager.games.game.colossalcombat.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.colossalcombat.ColossalCombatGame;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class GameOverState extends ColossalCombatStateBase {
    public GameOverState(@NotNull ColossalCombatGame context) {
        super(context);
        context.getAdminSidebar().addLine("over", "");
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getGameOverDuration())
                .withSidebar(context.getAdminSidebar(), "over")
                .sidebarPrefix(Component.text("Game Over: "))
                .withTopbar(context.getTopbar())
                .onCompletion(() -> {
                    context.getAdminSidebar().deleteLine("over");
                    context.stop();
                })
                .build());
    }
}
