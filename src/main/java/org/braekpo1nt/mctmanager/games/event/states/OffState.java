package org.braekpo1nt.mctmanager.games.event.states;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.event.EventManager;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.participant.Participant;
import org.braekpo1nt.mctmanager.participant.Team;
import org.braekpo1nt.mctmanager.ui.UIUtils;
import org.braekpo1nt.mctmanager.ui.sidebar.KeyLine;
import org.braekpo1nt.mctmanager.utils.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class OffState implements EventState {
    
    private final EventManager context;
    private final GameManager gameManager;
    
    public OffState(EventManager context) {
        this.context = context;
        this.gameManager = context.getGameManager();
    }
    
    @Override
    public void onParticipantJoin(Participant participant) {
        // do nothing
    }
    
    @Override
    public void onParticipantQuit(Participant participant) {
        // do nothing
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        // do nothing
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        // do nothing
    }
    
    @Override
    public void startEvent(@NotNull CommandSender sender, int numberOfGames, int currentGameNumber) {
        if (context.getGameManager().gameIsRunning()) {
            sender.sendMessage(Component.text("Can't start an event while a game is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (context.getGameManager().editorIsRunning()) {
            sender.sendMessage(Component.text("Can't start an event while an editor is running.")
                    .color(NamedTextColor.RED));
            return;
        }
        
        try {
            context.setConfig(context.getConfigController().getConfig());
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, e.getMessage(), e);
            sender.sendMessage(Component.text("Can't start event. Error loading config file. See console for details:\n")
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        
        context.getPlugin().getServer().getPluginManager().registerEvents(context, context.getPlugin());
        context.getGameManager().getTimerManager().register(context.getTimerManager());
        context.setWinningTeam(null);
        context.setMaxGames(numberOfGames);
        context.setCurrentGameNumber(currentGameNumber);
        context.getPlayedGames().clear();
        context.getScoreKeepers().clear();
        context.setSidebar(context.getGameManager().createSidebar());
        context.setAdmins(new ArrayList<>());
        context.setAdminSidebar(context.getGameManager().createSidebar());
        initializeSidebar();
        initializeAdminSidebar();
        context.initializeParticipantsAndAdmins();
        context.getGameManager().removeParticipantsFromHub(context.getParticipants());
        context.setState(new ReadyUpState(context));
    }
    
    private void initializeSidebar() {
        List<Team> sortedTeams = EventManager.sortTeams(gameManager.getTeams());
        context.setNumberOfTeams(sortedTeams.size());
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            Team team = sortedTeams.get(i);
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        context.getSidebar().addLine("currentGame", context.getCurrentGameLine());
        context.getSidebar().addLines(teamLines);
        context.getSidebar().addLine("personalScore", "");
        context.getSidebar().addLine("timer", "");
        context.getSidebar().updateTitle(context.getConfig().getTitle());
    }
    
    private void initializeAdminSidebar() {
        List<Team> sortedTeams = EventManager.sortTeams(gameManager.getTeams());
        context.setNumberOfTeams(sortedTeams.size());
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            Team team = sortedTeams.get(i);
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        context.getAdminSidebar().addLine("currentGame", context.getCurrentGameLine());
        context.getAdminSidebar().addLines(teamLines);
        context.getAdminSidebar().addLine("timer", "");
        context.getAdminSidebar().updateTitle(context.getConfig().getTitle());
    }
    
    @Override
    public void updatePersonalScores(Collection<Participant> updateParticipants) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "OffState updatePersonalScores()");
        if (context.getSidebar() == null) {
            return;
        }
        for (Participant participant : updateParticipants) {
            context.getSidebar().updateLine(participant.getUniqueId(), "personalScore",
                    Component.empty()
                            .append(Component.text("Personal: "))
                            .append(Component.text(participant.getScore()))
                            .color(NamedTextColor.GOLD));
        }
    }
    
    @Override
    public <T extends Team> void updateTeamScores(Collection<T> updateTeams) {
        Main.debugLog(LogType.EVENT_UPDATE_SCORES, "OffState updateTeamScores()");
        if (context.getSidebar() == null) {
            return;
        }
        List<Team> sortedTeams = EventManager.sortTeams(updateTeams);
        if (context.getNumberOfTeams() != sortedTeams.size()) {
            EventState.reorderTeamLines(sortedTeams, context);
            return;
        }
        KeyLine[] teamLines = new KeyLine[context.getNumberOfTeams()];
        for (int i = 0; i < context.getNumberOfTeams(); i++) {
            Team team = sortedTeams.get(i);
            teamLines[i] = new KeyLine("team"+i, Component.empty()
                    .append(team.getFormattedDisplayName())
                    .append(Component.text(": "))
                    .append(Component.text(team.getScore())
                            .color(NamedTextColor.GOLD))
            );
        }
        context.getSidebar().updateLines(teamLines);
        if (context.getAdminSidebar() == null) {
            return;
        }
        context.getAdminSidebar().updateLines(teamLines);
    }
    
    @Override
    public void onParticipantDamage(EntityDamageEvent event) {
        // do nothing
    }
    
    @Override
    public void onClickInventory(InventoryClickEvent event, Participant participant) {
        // do nothing
    }
    
    @Override
    public void onDropItem(PlayerDropItemEvent event, @NotNull Participant participant) {
        // do nothing
    }
    
    @Override
    public void gameIsOver(@NotNull GameType finishedGameType) {
        // do nothing
    }
    
    @Override
    public void colossalCombatIsOver(@Nullable Team winningTeam) {
        if (winningTeam == null) {
            Component message = Component.text("No winner declared.");
            context.messageAllAdmins(message);
            gameManager.messageOnlineParticipants(message);
            gameManager.returnAllParticipantsToHub();
            return;
        }
        Component message = Component.empty()
                .append(winningTeam.getFormattedDisplayName())
                .append(Component.text(" wins!"))
                .color(winningTeam.getColor())
                .decorate(TextDecoration.BOLD);
        context.messageAllAdmins(message);
        gameManager.messageOnlineParticipants(message);
        Audience.audience(
                Audience.audience(context.getAdmins()),
                Audience.audience(gameManager.getOnlineParticipants())
        ).showTitle(Title.title(winningTeam.getFormattedDisplayName(), Component.text("wins!")
                .color(winningTeam.getColor()), UIUtils.DEFAULT_TIMES));
    }
    
    @Override
    public void setMaxGames(@NotNull CommandSender sender, int newMaxGames) {
        sender.sendMessage(Component.text("There is no event running")
                .color(NamedTextColor.RED));
    }
    
    @Override
    public void stopColossalCombat(@NotNull CommandSender sender) {
        if (!context.getColossalCombatGame().isActive()) {
            sender.sendMessage(Component.text("Colossal Combat is not running")
                    .color(NamedTextColor.RED));
            return;
        }
        context.getColossalCombatGame().stop(null);
    }
    
    @Override
    public void startColossalCombat(@NotNull CommandSender sender, @NotNull Team firstTeam, @NotNull Team secondTeam) {
        if (context.getGameManager().gameIsRunning()) {
            sender.sendMessage(Component.text("Can't start Colossal Combat while a game is running")
                    .color(NamedTextColor.RED));
            return;
        }
        if (context.getColossalCombatGame().isActive()) {
            sender.sendMessage(Component.text("Colossal Combat is already running")
                    .color(NamedTextColor.RED));
            return;
        }
        try {
            context.getColossalCombatGame().loadConfig(context.getColossalCombatConfigFile());
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, e.getMessage(), e);
            sender.sendMessage(Component.text("Error loading config file. See console for details.")
                    .color(NamedTextColor.RED));
            context.messageAllAdmins(Component.text("Can't start ")
                    .append(Component.text("Colossal Combat")
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(". Error loading config file. See console for details:\n"))
                    .append(Component.text(e.getMessage()))
                    .color(NamedTextColor.RED));
            return;
        }
        List<Participant> firstPlaceParticipants = new ArrayList<>();
        List<Participant> secondPlaceParticipants = new ArrayList<>();
        List<Participant> spectators = new ArrayList<>();
        List<Participant> participantPool = new ArrayList<>(gameManager.getOnlineParticipants());
        List<Player> adminPool;
        adminPool = new ArrayList<>(gameManager.getOnlineAdmins());
        for (Participant participant : participantPool) {
            if (participant.getTeamId().equals(firstTeam.getTeamId())) {
                firstPlaceParticipants.add(participant);
            } else if (participant.getTeamId().equals(secondTeam.getTeamId())) {
                secondPlaceParticipants.add(participant);
            } else {
                spectators.add(participant);
            }
        }
        
        if (firstPlaceParticipants.isEmpty()) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("There are no members of the first place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstTeam.getTeamId(), secondTeam.getTeamId())))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return;
        }
        
        if (secondPlaceParticipants.isEmpty()) {
            sender.sendMessage(Component.empty()
                    .append(Component.text("There are no members of the second place team online. Please use "))
                    .append(Component.text("/mct event finalgame start <first> <second>")
                            .clickEvent(ClickEvent.suggestCommand(String.format("/mct event finalgame start %s %s", firstTeam.getTeamId(), secondTeam.getTeamId())))
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(" to manually start the final game."))
                    .color(NamedTextColor.RED));
            return;
        }
        
        gameManager.removeParticipantsFromHub(participantPool);
        context.getColossalCombatGame().start(firstTeam, secondTeam, firstPlaceParticipants, secondPlaceParticipants, spectators, adminPool);
    }
    
    @Override
    public void readyUpParticipant(@NotNull Participant participant) {
        participant.sendMessage(Component.text("There is no event going on right now"));
    }
    
    @Override
    public void unReadyParticipant(@NotNull Participant participant) {
        participant.sendMessage(Component.text("There is no event going on right now"));
    }
    
    @Override
    public void listReady(@NotNull CommandSender sender, @Nullable String teamId) {
        sender.sendMessage(Component.text("There is no event going on right now"));
    }
}
