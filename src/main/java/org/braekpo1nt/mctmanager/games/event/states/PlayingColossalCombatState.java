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
    
    public PlayingColossalCombatState(EventManager context, @NotNull Team firstTeam, @NotNull Team secondTeam) {
        super(context);
        boolean success = tryToStartColossalCombat(firstTeam, secondTeam);
        if (!success) {
            context.setState(new WaitingInHubState(context));
        }
    }
    
    @Override
    protected void startGame(EventManager context, @NotNull GameType gameType, @NotNull String configFile) {
        // do nothing
    }
    
    private boolean tryToStartColossalCombat(@NotNull Team firstTeam, @NotNull Team secondTeam) {
        try {
            context.getColossalCombatGame().loadConfig(context.getConfig().getColossalCombatConfig());
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, "Error trying to start Colossal Combat", e);
            context.messageAllAdmins(Component.text("Can't start ")
                    .append(Component.text("Colossal Combat")
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return false;
        }
        List<Participant> firstPlaceParticipants = new ArrayList<>();
        List<Participant> secondPlaceParticipants = new ArrayList<>();
        List<Participant> spectators = new ArrayList<>();
        for (Participant participant : context.getParticipants()) {
            String teamId = participant.getTeamId();
            if (teamId.equals(firstTeam.getTeamId())) {
                firstPlaceParticipants.add(participant);
            } else if (teamId.equals(secondTeam.getTeamId())) {
                secondPlaceParticipants.add(participant);
            } else {
                spectators.add(participant);
            }
        }
        
        if (firstPlaceParticipants.isEmpty()) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are no members of the first place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstTeam.getTeamId(), secondTeam.getTeamId())))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (secondPlaceParticipants.isEmpty()) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are no members of the second place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstTeam.getTeamId(), secondTeam.getTeamId())))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return false;
        }
        context.getSidebar().removePlayers(context.getParticipants());
        context.getAdminSidebar().removePlayers(context.getAdmins());
        gameManager.removeParticipantsFromHub(context.getParticipants());
        context.getColossalCombatGame().start(firstTeam, secondTeam, firstPlaceParticipants, secondPlaceParticipants, spectators, context.getAdmins());
        context.getAdmins().clear();
        return true;
    }
    
    @Override
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
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull Team firstTeam, @NotNull Team secondTeam) {
        sender.sendMessage(Component.text("Colossal Combat is already running").color(NamedTextColor.RED));
    }
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        context.getColossalCombatGame().stop(null);
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        context.getColossalCombatGame().onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        context.getColossalCombatGame().onParticipantQuit(participant);
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        context.getColossalCombatGame().onAdminJoin(admin);
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        context.getColossalCombatGame().onAdminQuit(admin);
    }
}
