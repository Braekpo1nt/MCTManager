package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.delay.ToPodiumDelayState;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PlayingColossalCombatState extends PlayingGameState {
    
    public PlayingColossalCombatState(EventManager context, @NotNull Team firstTeam, @NotNull Team secondTeam, @NotNull String configFile) {
        super(context);
    }
    
    @Override
    protected void startGame(EventManager context, @NotNull GameType gameType, @NotNull String configFile) {
        // do nothing
    }
    
    public void colossalCombatIsOver(@Nullable Team winningTeam) {
        if (winningTeam == null) {
            Component message = Component.text("Game stopped early. No winner declared.");
            context.messageAllAdmins(message);
            gameManager.messageOnlineParticipants(message);
            context.setWinningTeam(null);
            context.initializeParticipantsAndAdmins();
            context.setState(new ToPodiumDelayState(context));
            return;
        }
        Component message = Component.empty()
                .append(winningTeam.getFormattedDisplayName())
                .append(Component.text(" wins ")
                        .append(context.getConfig().getTitle())
                        .append(Component.text("!")))
                .color(winningTeam.getColor())
                .decorate(TextDecoration.BOLD);
        context.getPlugin().getServer().sendMessage(message);
        context.getPlugin().getServer().showTitle(Title.title(
                winningTeam.getFormattedDisplayName(),
                Component.empty()
                        .append(Component.text("wins "))
                        .append(context.getConfig().getTitle())
                        .append(Component.text("!"))
                        .color(winningTeam.getColor()),
                UIUtils.DEFAULT_TIMES));
        context.setWinningTeam(winningTeam);
        context.initializeParticipantsAndAdmins();
        context.setState(new ToPodiumDelayState(context));
    }
}
