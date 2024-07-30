package org.braekpo1nt.mctmanager.games.event.states.delay;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.ui.sidebar.Sidebar;
import org.braekpo1nt.mctmanager.ui.timer.Timer;
import org.bukkit.entity.Player;

public class ToPodiumDelayState extends DelayState {
    public ToPodiumDelayState(EventManager context, String winningTeam) {
        super(context);
        GameManager gameManager = context.getGameManager();
        Sidebar sidebar = context.getSidebar();
        Sidebar adminSidebar = context.getAdminSidebar();
        context.setWinningTeam(winningTeam);
        context.initializeParticipantsAndAdmins();
        Component teamDisplayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        Component winner = Component.empty()
                .append(Component.text("Winner: "))
                .append(teamDisplayName);
        context.getTimerManager().start(Timer.builder()
                .duration(context.getConfig().getBackToHubDuration())
                .withSidebar(sidebar, "timer")
                .withSidebar(adminSidebar, "timer")
                .sidebarPrefix(Component.text("Heading to Podium: "))
                .onCompletion(() -> {
                    sidebar.addLine("winner", winner);
                    adminSidebar.addLine("winner", winner);
                    sidebar.updateLine("currentGame", context.getCurrentGameLine());
                    adminSidebar.updateLine("currentGame", context.getCurrentGameLine());
                    gameManager.returnAllParticipantsToPodium(winningTeam);
                    for (Player participant : context.getParticipants()) {
                        String team = gameManager.getTeamName(participant.getUniqueId());
                        if (team.equals(winningTeam)) {
                            context.giveCrown(participant);
                        }
                    }
                })
                .build());
    }
}
