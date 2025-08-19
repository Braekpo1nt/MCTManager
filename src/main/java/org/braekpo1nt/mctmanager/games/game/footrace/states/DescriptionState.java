package org.braekpo1nt.mctmanager.games.game.footrace.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceParticipant;
import org.braekpo1nt.mctmanager.games.game.footrace.FootRaceTeam;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionState extends FootRaceStateBase {
    
    private @Nullable Timer timer;
    
    public DescriptionState(@NotNull FootRaceGame context) {
        super(context);
    }
    
    @Override
    public void enter() {
        context.messageAllParticipants(context.getConfig().getDescription());
        timer = context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getDescriptionDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Starting soon: "))
                .onCompletion(() -> context.setState(new StartingState(context)))
                .build());
    }
    
    @Override
    public void exit() {
        Timer.cancel(timer);
    }
    
    @Override
    public void onParticipantRejoin(FootRaceParticipant participant, FootRaceTeam team) {
        super.onParticipantRejoin(participant, team);
        context.getSidebar().updateLine("elapsedTime", "00:00:000");
    }
    
    @Override
    public void onNewParticipantJoin(FootRaceParticipant participant, FootRaceTeam team) {
        super.onNewParticipantJoin(participant, team);
        context.getSidebar().updateLine("elapsedTime", "00:00:000");
    }
}
