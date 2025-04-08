package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class RoundOverState extends ClockworkStateBase {
    public RoundOverState(@NotNull ClockworkGame context) {
        super(context);
        for (ClockworkParticipant participant : context.getParticipants().values()) {
            participant.setGameMode(GameMode.SPECTATOR);
            ParticipantInitializer.clearInventory(participant);
            participant.setAlive(true);
        }
        context.titleAllParticipants(UIUtils.roundOverTitle());
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundOverDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Round Over: "))
                .onCompletion(() -> {
                    context.stopInvisible();
                    if (context.getCurrentRound() < context.getConfig().getRounds()) {
                        context.setCurrentRound(context.getCurrentRound() + 1);
                        context.setState(new PreRoundState(context));
                    } else {
                        context.setState(new GameOverState(context));
                    }
                })
                .build());
    }
}
