package org.braekpo1nt.mctmanager.games.game.clockwork.states;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkGame;
import org.braekpo1nt.mctmanager.games.game.clockwork.ClockworkTeam;
import org.braekpo1nt.mctmanager.games.game.clockwork.config.ClockworkConfig;
import org.braekpo1nt.mctmanager.games.game.clockwork_old.ClockworkRoundTeam;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StayOnWedgeState extends ClockworkStateBase {
    private final @NotNull ClockworkConfig config;
    
    public StayOnWedgeState(@NotNull ClockworkGame context) {
        super(context);
        this.config = context.getConfig();
        context.getTimerManager().start(Timer.builder()
                .duration(config.getStayOnWedgeDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Stay on wedge: "))
                .onCompletion(() -> {
                    List<ClockworkTeam> livingTeams = context.getTeams().values().stream()
                            .filter(ClockworkTeam::isAlive).toList();
                    if (livingTeams.size() == 1) {
                        onTeamWinsRound(livingTeams.getFirst());
                    } else {
                        incrementChaos();
                        startBreatherDelay();
                    }
                })
                .name("startStayOnWedgeDelay")
                .build());
        killParticipantsNotOnWedge();
    }
}
