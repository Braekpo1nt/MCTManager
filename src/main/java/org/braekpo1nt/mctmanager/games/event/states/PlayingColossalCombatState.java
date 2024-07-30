package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.delay.ToPodiumDelayState;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class PlayingColossalCombatState extends PlayingGameState {
    public PlayingColossalCombatState(EventManager context, String firstPlace, String secondPlace) {
        super(context);
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable String winningTeam) {
        if (winningTeam == null) {
            Component message = Component.text("No winner declared.");
            context.messageAllAdmins(message);
            gameManager.messageOnlineParticipants(message);
            context.initializeParticipantsAndAdmins();
            context.setState(new WaitingInHubState(context));
            return;
        }
        NamedTextColor teamColor = gameManager.getTeamNamedTextColor(winningTeam);
        Component formattedTeamDisplayName = gameManager.getFormattedTeamDisplayName(winningTeam);
        Component message = Component.empty()
                .append(formattedTeamDisplayName)
                .append(Component.text(" wins ")
                        .append(context.getConfig().getTitle())
                        .append(Component.text("!")))
                .color(teamColor)
                .decorate(TextDecoration.BOLD);
        Bukkit.getServer().sendMessage(message);
        Bukkit.getServer().showTitle(Title.title(
                formattedTeamDisplayName,
                Component.empty()
                        .append(Component.text("wins "))
                        .append(context.getConfig().getTitle())
                        .append(Component.text("!"))
                        .color(teamColor),
                UIUtils.DEFAULT_TIMES));
        context.setState(new ToPodiumDelayState(context, winningTeam));
    }
}
