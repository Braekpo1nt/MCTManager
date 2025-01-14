package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameOverState implements FootRaceState {
    
    private final @NotNull FootRaceGame context;
    
    public GameOverState(@NotNull FootRaceGame context) {
        this.context = context;
        Audience.audience(context.getParticipants()).showTitle(UIUtils.gameOverTitle());
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
    
    @Override
    public void onParticipantJoin(Player participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        resetParticipant(participant);
        context.getParticipants().remove(participant);
    }
    
    @Override
    public void initializeParticipant(Participant participant) {
        context.initializeParticipant(participant);
    }
    
    @Override
    public void resetParticipant(Participant participant) {
        context.resetParticipant(participant);
    }
    
    @Override
    public void onParticipantMove(Player participant) {
        // do nothing
    }
}
