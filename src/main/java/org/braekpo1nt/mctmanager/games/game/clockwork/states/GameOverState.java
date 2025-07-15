package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameOverState extends ClockworkStateBase {
    
    private @Nullable Timer timer;
    
    public GameOverState(@NotNull ClockworkGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        for (ClockworkParticipant participant : context.getParticipants().values()) {
            participant.setGameMode(GameMode.SPECTATOR);
            ParticipantInitializer.clearInventory(participant);
            participant.setAlive(true);
        }
        context.titleAllParticipants(UIUtils.gameOverTitle());
        context.getSidebar().updateLine("round", Component.empty());
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundOverDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Game Over: "))
                .onCompletion(context::stop)
                .build());
    }
    
    @Override
    public void exit() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
