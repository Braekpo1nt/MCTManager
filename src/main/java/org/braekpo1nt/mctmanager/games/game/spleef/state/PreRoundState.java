package org.braekpo1nt.mctmanager.games.game.spleef.state;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefGame;
import org.braekpo1nt.mctmanager.games.game.spleef.SpleefParticipant;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

public class PreRoundState extends SpleefStateBase {
    public PreRoundState(@NotNull SpleefGame context) {
        super(context);
        for (SpleefParticipant participant : context.getParticipants().values()) {
            context.teleportToRandomStartingPosition(participant);
            ParticipantInitializer.clearInventory(participant);
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
                .onCompletion(() -> context.setState(new RoundActiveState(context)))
                .build());
    }
}
