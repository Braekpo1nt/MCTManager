package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.ScoreKeeper;
import org.braekpo1nt.mctmanager.games.event.states.PlayingGameState;
import org.braekpo1nt.mctmanager.games.event.states.WaitingInHubState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StartingGameDelayState extends DelayState {
    public StartingGameDelayState(EventManager context, GameType gameType) {
        super(context);
        context.initializeParticipantsAndAdmins();
        GameManager gameManager = context.getGameManager();
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
