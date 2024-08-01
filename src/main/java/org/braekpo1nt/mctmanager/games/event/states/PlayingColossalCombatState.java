package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.event.states.delay.ToPodiumDelayState;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlayingColossalCombatState extends PlayingGameState {
    
    public PlayingColossalCombatState(EventManager context, @NotNull String firstTeamId, @NotNull String secondTeamId) {
        super(context);
        boolean success = tryToStartColossalCombat(firstTeamId, secondTeamId);
        if (!success) {
            context.setState(new WaitingInHubState(context));
        }
    }
    
    private boolean tryToStartColossalCombat(@NotNull String firstTeamId, @NotNull String secondTeamId) {
        try {
            context.getColossalCombatGame().loadConfig();
        } catch (ConfigException e) {
            Bukkit.getLogger().severe(e.getMessage());
            e.printStackTrace();
            context.messageAllAdmins(Component.text("Can't start ")
                    .append(Component.text("Colossal Combat")
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return false;
        }
        List<Player> firstPlaceParticipants = new ArrayList<>();
        List<Player> secondPlaceParticipants = new ArrayList<>();
        List<Player> spectators = new ArrayList<>();
        for (Player participant : context.getParticipants()) {
            String teamName = gameManager.getTeamName(participant.getUniqueId());
            if (teamName.equals(firstTeamId)) {
                firstPlaceParticipants.add(participant);
            } else if (teamName.equals(secondTeamId)) {
                secondPlaceParticipants.add(participant);
            } else {
                spectators.add(participant);
            }
        }
        
        if (firstPlaceParticipants.isEmpty()) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are no members of the first place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstTeamId, secondTeamId)))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return false;
        }
        
        if (secondPlaceParticipants.isEmpty()) {
            context.messageAllAdmins(Component.empty()
                    .append(Component.text("There are no members of the second place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstTeamId, secondTeamId)))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return false;
        }
        context.getSidebar().removePlayers(context.getParticipants());
        context.getAdminSidebar().removePlayers(context.getAdmins());
        gameManager.removeParticipantsFromHub(context.getParticipants());
        context.getColossalCombatGame().start(firstPlaceParticipants, secondPlaceParticipants, spectators, context.getAdmins());
        context.getParticipants().clear();
        context.getAdmins().clear();
        return true;
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable String winningTeam) {
        if (winningTeam == null) {
            Component message = Component.text("Game stopped early. No winner declared.");
            context.messageAllAdmins(message);
            gameManager.messageOnlineParticipants(message);
            context.setWinningTeam(null);
            context.setState(new ToPodiumDelayState(context));
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
        context.getPlugin().getServer().sendMessage(message);
        context.getPlugin().getServer().showTitle(Title.title(
                formattedTeamDisplayName,
                Component.empty()
                        .append(Component.text("wins "))
                        .append(context.getConfig().getTitle())
                        .append(Component.text("!"))
                        .color(teamColor),
                UIUtils.DEFAULT_TIMES));
        context.setWinningTeam(winningTeam);
        context.setState(new ToPodiumDelayState(context));
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull String firstTeam, @NotNull String secondTeam) {
        sender.sendMessage(Component.text("Colossal Combat is already running").color(NamedTextColor.RED));
    }
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        context.getColossalCombatGame().stop(null);
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        context.getColossalCombatGame().onParticipantJoin(participant);
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
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
