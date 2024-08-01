package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.PlayingColossalCombatState;
import org.braekpo1nt.mctmanager.games.event.states.WaitingInHubState;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;

import java.util.*;

public class ToColossalCombatDelay extends DelayState {
    
    private final GameManager gameManager;
    
    public ToColossalCombatDelay(EventManager context) {
        super(context);
        this.gameManager = context.getGameManager();
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getStartingGameDuration())
                .withSidebar(context.getSidebar(), "timer")
                .withSidebar(context.getAdminSidebar(), "timer")
                .sidebarPrefix(Component.text("Colossal Combat: "))
                .onCompletion(() -> {
                    boolean startedColossalCombat = identifyWinnersAndStartColossalCombat();
                    if (!startedColossalCombat) {
                        context.messageAllAdmins(Component.text("Unable to start Colossal Combat. Returning to waiting in hub state."));
                        context.setState(new WaitingInHubState(context));
                    }
                })
                .build());
    }
    
    /**
     * @return true if two teams were picked and Colossal Combat started successfully. False if anything went wrong.
     */
    private boolean identifyWinnersAndStartColossalCombat() {
        Set<String> allTeams = gameManager.getTeamNames();
        if (allTeams.size() < 2) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are fewer than two teams online. Use "))
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams.")));
            return false;
        }
        Map<String, Integer> teamScores = new HashMap<>();
        for (String teamName : allTeams) {
            int score = gameManager.getScore(teamName);
            teamScores.put(teamName, score);
        }
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(teamScores);
        if (firstPlaces.length == 2) {
            String firstPlace = firstPlaces[0];
            String secondPlace = firstPlaces[1];
            context.setState(new PlayingColossalCombatState(
                    context,
                    firstPlace,
                    secondPlace));
            return true;
        }
        if (firstPlaces.length > 2) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are more than 2 teams tied for first place. A tie breaker is needed. Use "))
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams."))
                    .color(NamedTextColor.RED));
            return false;
        }
        String firstPlace = firstPlaces[0];
        teamScores.remove(firstPlace);
        String[] secondPlaces = GameManagerUtils.calculateFirstPlace(teamScores);
        if (secondPlaces.length > 1) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There is a tie second place. A tie breaker is needed. Use "))
                    .append(Component.text("/mct game finalgame <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand("/mct game finalgame "))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to start the final game with the two chosen teams."))
                    .color(NamedTextColor.RED));
            return false;
        }
        String secondPlace = secondPlaces[0];
        int onlineFirsts = 0;
        int onlineSeconds = 0;
        for (Player participant : context.getParticipants()) {
            String team = gameManager.getTeamName(participant.getUniqueId());
            if (team.equals(firstPlace)) {
                onlineFirsts++;
            }
            if (team.equals(secondPlace)) {
                onlineSeconds++;
            }
        }
        if (onlineFirsts <= 0) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are no members of the first place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstPlace, secondPlace)))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (onlineSeconds <= 0) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are no members of the second place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstPlace, secondPlace)))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return false;
        }
        
        context.setState(new PlayingColossalCombatState(
                context,
                firstPlace,
                secondPlace));
        return true;
    }
}
