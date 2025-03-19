package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.PlayingGameState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class StartingGameDelayState extends DelayState {
    public StartingGameDelayState(EventManager context, @NotNull GameType gameType, @NotNull String configFile) {
        super(context);
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getStartingGameDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.empty()
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(": ")))
                .onCompletion(() -> {
                    context.setState(new PlayingGameState(context, gameType, configFile));
                })
                .build());
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "StartingGameDelayState updatePersonalScores()----");
    }
    
    @Override
    public <T extends Team> void updateTeamScores(Collection<T> updateTeams) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "StartingGameDelayState updateTeamScores()----");
    }
}
