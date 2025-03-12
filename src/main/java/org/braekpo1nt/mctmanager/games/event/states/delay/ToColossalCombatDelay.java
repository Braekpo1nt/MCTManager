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
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.timer.Timer;

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
        // TODO: Teams Refactor this to use Teams instead of TeamIds
        Collection<Team> allTeams = gameManager.getTeams();
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
        for (Team team : allTeams) {
            teamScores.put(team.getTeamId(), team.getScore());
        }
        String[] firstPlaces = GameManagerUtils.calculateFirstPlace(teamScores);
        if (firstPlaces.length == 2) {
            Team firstPlace = Objects.requireNonNull(gameManager.getTeam(firstPlaces[0]), 
                    "teamId not found even though game manager produced it");
            Team secondPlace = Objects.requireNonNull(gameManager.getTeam(firstPlaces[1]), 
                    "teamId not found even though game manager produced it");
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
        Team firstPlace = Objects.requireNonNull(gameManager.getTeam(firstPlaces[0]),
                "teamId not found even though game manager produced it");
        teamScores.remove(firstPlace.getTeamId());
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
        Team secondPlace = Objects.requireNonNull(gameManager.getTeam(secondPlaces[0]),
                "teamId not found even though game manager produced it");
        int onlineFirsts = 0;
        int onlineSeconds = 0;
        for (Participant participant : context.getParticipants().values()) {
            if (participant.getTeamId().equals(firstPlace.getTeamId())) {
                onlineFirsts++;
            }
            if (participant.getTeamId().equals(secondPlace.getTeamId())) {
                onlineSeconds++;
            }
        }
        if (onlineFirsts <= 0) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are no members of the first place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstPlace.getTeamId(), secondPlace.getTeamId())))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (onlineSeconds <= 0) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are no members of the second place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstPlace.getTeamId(), secondPlace.getTeamId())))
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
