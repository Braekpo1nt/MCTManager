package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.PlayingGameState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;

public class StartingGameDelayState extends DelayState {
    public StartingGameDelayState(EventManager context, GameType gameType) {
        super(context);
        context.initializeParticipantsAndAdmins();
        Sidebar sidebar = context.getSidebar();
        Sidebar adminSidebar = context.getAdminSidebar();
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getStartingGameDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.empty()
                        .append(Component.text(gameType.getTitle()))
                        .append(Component.text(": ")))
                .onCompletion(() -> {
                    context.setState(new PlayingGameState(context, gameType));
                })
                .build());
    }
}
