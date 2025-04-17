package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.PlayingFinalGameState;
import org.braekpo1nt.mctmanager.games.event.states.WaitingInHubState;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.braekpo1nt.mctmanager.utils.LogType;

import java.util.*;

public class ToFinalGameDelay extends DelayState {
    
    public ToFinalGameDelay(EventManager context) {
        super(context);
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getStartingGameDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Colossal Combat: "))
                .onCompletion(() -> {
                    // TODO: improve the success or failure measurement of starting a game
                    try {
                        context.setState(new PlayingFinalGameState(context,
                                context.getConfig().getColossalCombatConfig()));
                    } catch (Exception e) {
                        context.messageAllAdmins(Component.text("Unable to start Colossal Combat. Returning to waiting in hub state."));
                        context.setState(new WaitingInHubState(context));
                    }
                })
                .build());
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "ToColossalCombatDelayState updatePersonalScores()----");
        // do nothing
    }
    
    @Override
    public <T extends Team> void updateTeamScores(Collection<T> updateTeams) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "ToColossalCombatDelayState updateTeamScores()----");
        // do nothing
    }
    
}
