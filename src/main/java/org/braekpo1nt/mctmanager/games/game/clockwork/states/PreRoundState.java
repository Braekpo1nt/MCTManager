package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class PreRoundState extends ClockworkStateBase {
    public PreRoundState(@NotNull ClockworkGame context) {
        super(context);
        for (ClockworkParticipant participant : context.getParticipants().values()) {
            participant.teleport(context.getConfig().getStartingLocation());
            ParticipantInitializer.clearInventory(participant);
            ParticipantInitializer.clearStatusEffects(participant);
            ParticipantInitializer.resetHealthAndHunger(participant);
            participant.setArrowsInBody(0);
            participant.setGameMode(GameMode.ADVENTURE);
        }
        Component roundLine = Component.empty()
                .append(Component.text("Round "))
                .append(Component.text(context.getCurrentRound()))
                .append(Component.text("/"))
                .append(Component.text(context.getConfig().getRounds()));
        context.getSidebar().updateLine("round", roundLine);
        context.getAdminSidebar().updateLine("round", roundLine);
        context.titleAllParticipants(UIUtils.roundXTitle(context.getCurrentRound()));
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getRoundStartingDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Round Starting: "))
                .onCompletion(() -> {
                    context.startInvisible();
                    context.getChaosManager().start(true);
                    context.setState(new BreatherState(context));
                })
                .build());
    }
}
