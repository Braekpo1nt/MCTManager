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
                    createScoreKeeperForGame(gameType);
                    for (Player participant : context.getParticipants()) {
                        sidebar.removePlayer(participant);
                    }
                    for (Player admin : context.getAdmins()) {
                        adminSidebar.removePlayer(admin);
                    }
                    context.getParticipants().clear();
                    context.getAdmins().clear();
                    boolean gameStarted = gameManager.startGame(gameType, Bukkit.getConsoleSender());
                    if (!gameStarted) {
                        context.messageAllAdmins(Component.text("Unable to start the game ")
                                .append(Component.text(gameType.getTitle()))
                                .append(Component.text(". Returning to the hub to try again."))
                                .color(NamedTextColor.RED));
                        context.initializeParticipantsAndAdmins();
                        context.setState(new WaitingInHubState(context));
                    }
                    context.setState(new PlayingGameState(context));
                })
                .build());
    }
    
    /**
     * Adds a ScoreKeeper to track the game's points. If no ScoreKeepers exist for gameType, creates a new list of iterations for the game.
     * @param gameType the game to add a ScoreKeeper for
     */
    private void createScoreKeeperForGame(GameType gameType) {
        if (!context.getScoreKeepers().containsKey(gameType)) {
            List<ScoreKeeper> iterationScoreKeepers = new ArrayList<>(List.of(new ScoreKeeper()));
            context.getScoreKeepers().put(gameType, iterationScoreKeepers);
        } else {
            List<ScoreKeeper> iterationScoreKeepers = context.getScoreKeepers().get(gameType);
            iterationScoreKeepers.add(new ScoreKeeper());
        }
    }
}
