package org.braekpo1nt.mctmanager.games.game.farmrush.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.commands.dynamic.top.TopCommand;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushGame;
import org.braekpo1nt.mctmanager.games.game.farmrush.FarmRushParticipant;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

public class GameOverState extends FarmRushStateBase {
    
    public GameOverState(@NotNull FarmRushGame context) {
        super(context);
        context.getPowerupManager().stop();
        TopCommand.setEnabled(false);
        Audience.audience(context.getParticipants().values().stream().map(Participant::getPlayer).toList()).showTitle(UIUtils.gameOverTitle());
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
